/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldArea
 *  net.runelite.api.coords.WorldPoint
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.api.movement.pathfinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.movement.pathfinder.CollisionMap;
import net.unethicalite.api.movement.pathfinder.model.MovementConstants;
import net.unethicalite.api.movement.pathfinder.model.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pathfinder
implements Callable<List<WorldPoint>> {
    private static final Logger log = LoggerFactory.getLogger(Pathfinder.class);
    final CollisionMap map;
    final Map<WorldPoint, List<Transport>> transports;
    private List<Node> start;
    private WorldArea target;
    private List<WorldPoint> targetTiles;
    private final List<Node> boundary = new LinkedList<Node>();
    private final Set<WorldPoint> visited = new HashSet<WorldPoint>();
    private Node nearest;
    boolean avoidWilderness;

    private static boolean isInWilderness(WorldPoint location) {
        return location.isInArea2D(new WorldArea[]{MovementConstants.WILDERNESS_ABOVE_GROUND, MovementConstants.WILDERNESS_UNDERGROUND});
    }

    public Pathfinder(CollisionMap collisionMap, Map<WorldPoint, List<Transport>> transports, List<WorldPoint> start, WorldPoint target, boolean avoidWilderness) {
        this(collisionMap, transports, start, target.toWorldArea(), avoidWilderness);
    }

    public Pathfinder(CollisionMap collisionMap, Map<WorldPoint, List<Transport>> transports, List<WorldPoint> start, WorldArea target, boolean avoidWilderness) {
        this.map = collisionMap;
        this.transports = transports;
        this.target = target;
        this.targetTiles = target.toWorldPointList();
        this.start = new ArrayList<Node>();
        this.start.addAll(start.stream().map(point -> new Node((WorldPoint)point, null)).collect(Collectors.toList()));
        this.nearest = null;
        this.avoidWilderness = avoidWilderness;
        if (this.targetTiles.stream().allMatch(collisionMap::fullBlock)) {
            log.warn("Walking to a {}, pathfinder will be slow", (Object)(this.targetTiles.size() == 1 ? "blocked tile" : "fully blocked area"));
        }
    }

    private void addNeighbors(Node node) {
        int plane;
        int y;
        int x = node.position.getX();
        if (this.map.w(x, y = node.position.getY(), plane = node.position.getPlane())) {
            this.addNeighbor(node, new WorldPoint(x - 1, y, plane));
        }
        if (this.map.e(x, y, plane)) {
            this.addNeighbor(node, new WorldPoint(x + 1, y, plane));
        }
        if (this.map.s(x, y, plane)) {
            this.addNeighbor(node, new WorldPoint(x, y - 1, plane));
        }
        if (this.map.n(x, y, plane)) {
            this.addNeighbor(node, new WorldPoint(x, y + 1, plane));
        }
        if (this.map.sw(x, y, plane)) {
            this.addNeighbor(node, new WorldPoint(x - 1, y - 1, plane));
        }
        if (this.map.se(x, y, plane)) {
            this.addNeighbor(node, new WorldPoint(x + 1, y - 1, plane));
        }
        if (this.map.nw(x, y, plane)) {
            this.addNeighbor(node, new WorldPoint(x - 1, y + 1, plane));
        }
        if (this.map.ne(x, y, plane)) {
            this.addNeighbor(node, new WorldPoint(x + 1, y + 1, plane));
        }
        for (Transport transport : (List)this.transports.getOrDefault(node.position, new ArrayList())) {
            this.addNeighbor(node, transport.getDestination());
        }
    }

    private void addNeighbor(Node node, WorldPoint neighbor) {
        if (this.avoidWilderness && Pathfinder.isInWilderness(neighbor) && !Pathfinder.isInWilderness(node.position) && this.targetTiles.stream().noneMatch(Pathfinder::isInWilderness)) {
            return;
        }
        if (!this.visited.add(neighbor)) {
            return;
        }
        this.boundary.add(new Node(neighbor, node));
    }

    public List<WorldPoint> find() {
        long startTime = System.currentTimeMillis();
        List<WorldPoint> path = this.find(5000000);
        String targetStr = this.targetTiles.size() == 1 ? this.target.toWorldPoint().toString() : String.format("WorldArea(x=%s, y=%s, width=%s, height=%s, plane=%s)", this.target.getX(), this.target.getY(), this.target.getWidth(), this.target.getHeight(), this.target.getPlane());
        log.debug("Path calculation took {} ms to {}", (Object)(System.currentTimeMillis() - startTime), (Object)targetStr);
        return path;
    }

    public List<WorldPoint> find(int maxSearch) {
        this.boundary.addAll(this.start);
        int bestDistance = Integer.MAX_VALUE;
        while (!this.boundary.isEmpty()) {
            if (Thread.interrupted()) {
                return List.of();
            }
            if (this.visited.size() >= maxSearch) {
                return this.nearest.path();
            }
            Node node = this.boundary.remove(0);
            if (this.target.contains(node.position)) {
                return node.path();
            }
            int distance = this.target.distanceTo(node.position);
            if (this.nearest == null || distance < bestDistance) {
                this.nearest = node;
                bestDistance = distance;
            }
            this.addNeighbors(node);
        }
        if (this.nearest != null) {
            return this.nearest.path();
        }
        return List.of();
    }

    @Override
    public List<WorldPoint> call() throws Exception {
        return this.find();
    }

    public CollisionMap getMap() {
        return this.map;
    }

    public Map<WorldPoint, List<Transport>> getTransports() {
        return this.transports;
    }

    public List<Node> getStart() {
        return this.start;
    }

    public WorldArea getTarget() {
        return this.target;
    }

    public List<WorldPoint> getTargetTiles() {
        return this.targetTiles;
    }

    public List<Node> getBoundary() {
        return this.boundary;
    }

    public Set<WorldPoint> getVisited() {
        return this.visited;
    }

    public Node getNearest() {
        return this.nearest;
    }

    public boolean isAvoidWilderness() {
        return this.avoidWilderness;
    }

    public void setStart(List<Node> start) {
        this.start = start;
    }

    public void setTarget(WorldArea target) {
        this.target = target;
    }

    public void setTargetTiles(List<WorldPoint> targetTiles) {
        this.targetTiles = targetTiles;
    }

    public void setNearest(Node nearest) {
        this.nearest = nearest;
    }

    public void setAvoidWilderness(boolean avoidWilderness) {
        this.avoidWilderness = avoidWilderness;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Pathfinder)) {
            return false;
        }
        Pathfinder other = (Pathfinder)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.isAvoidWilderness() != other.isAvoidWilderness()) {
            return false;
        }
        CollisionMap this$map = this.getMap();
        CollisionMap other$map = other.getMap();
        if (this$map == null ? other$map != null : !this$map.equals(other$map)) {
            return false;
        }
        Map<WorldPoint, List<Transport>> this$transports = this.getTransports();
        Map<WorldPoint, List<Transport>> other$transports = other.getTransports();
        if (this$transports == null ? other$transports != null : !((Object)this$transports).equals(other$transports)) {
            return false;
        }
        List<Node> this$start = this.getStart();
        List<Node> other$start = other.getStart();
        if (this$start == null ? other$start != null : !((Object)this$start).equals(other$start)) {
            return false;
        }
        WorldArea this$target = this.getTarget();
        WorldArea other$target = other.getTarget();
        if (this$target == null ? other$target != null : !this$target.equals(other$target)) {
            return false;
        }
        List<WorldPoint> this$targetTiles = this.getTargetTiles();
        List<WorldPoint> other$targetTiles = other.getTargetTiles();
        if (this$targetTiles == null ? other$targetTiles != null : !((Object)this$targetTiles).equals(other$targetTiles)) {
            return false;
        }
        List<Node> this$boundary = this.getBoundary();
        List<Node> other$boundary = other.getBoundary();
        if (this$boundary == null ? other$boundary != null : !((Object)this$boundary).equals(other$boundary)) {
            return false;
        }
        Set<WorldPoint> this$visited = this.getVisited();
        Set<WorldPoint> other$visited = other.getVisited();
        if (this$visited == null ? other$visited != null : !((Object)this$visited).equals(other$visited)) {
            return false;
        }
        Node this$nearest = this.getNearest();
        Node other$nearest = other.getNearest();
        return !(this$nearest == null ? other$nearest != null : !((Object)this$nearest).equals(other$nearest));
    }

    protected boolean canEqual(Object other) {
        return other instanceof Pathfinder;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + (this.isAvoidWilderness() ? 79 : 97);
        CollisionMap $map = this.getMap();
        result = result * 59 + ($map == null ? 43 : $map.hashCode());
        Map<WorldPoint, List<Transport>> $transports = this.getTransports();
        result = result * 59 + ($transports == null ? 43 : ((Object)$transports).hashCode());
        List<Node> $start = this.getStart();
        result = result * 59 + ($start == null ? 43 : ((Object)$start).hashCode());
        WorldArea $target = this.getTarget();
        result = result * 59 + ($target == null ? 43 : $target.hashCode());
        List<WorldPoint> $targetTiles = this.getTargetTiles();
        result = result * 59 + ($targetTiles == null ? 43 : ((Object)$targetTiles).hashCode());
        List<Node> $boundary = this.getBoundary();
        result = result * 59 + ($boundary == null ? 43 : ((Object)$boundary).hashCode());
        Set<WorldPoint> $visited = this.getVisited();
        result = result * 59 + ($visited == null ? 43 : ((Object)$visited).hashCode());
        Node $nearest = this.getNearest();
        result = result * 59 + ($nearest == null ? 43 : ((Object)$nearest).hashCode());
        return result;
    }

    public String toString() {
        return "Pathfinder(map=" + String.valueOf(this.getMap()) + ", transports=" + String.valueOf(this.getTransports()) + ", start=" + String.valueOf(this.getStart()) + ", target=" + String.valueOf(this.getTarget()) + ", targetTiles=" + String.valueOf(this.getTargetTiles()) + ", boundary=" + String.valueOf(this.getBoundary()) + ", visited=" + String.valueOf(this.getVisited()) + ", nearest=" + String.valueOf(this.getNearest()) + ", avoidWilderness=" + this.isAvoidWilderness() + ")";
    }

    private static final class Node {
        private final WorldPoint position;
        private final Node previous;

        public List<WorldPoint> path() {
            LinkedList<WorldPoint> path = new LinkedList<WorldPoint>();
            Node node = this;
            while (node != null) {
                path.add(0, node.position);
                node = node.previous;
            }
            return new ArrayList<WorldPoint>(path);
        }

        public Node(WorldPoint position, Node previous) {
            this.position = position;
            this.previous = previous;
        }

        public WorldPoint getPosition() {
            return this.position;
        }

        public Node getPrevious() {
            return this.previous;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Node)) {
                return false;
            }
            Node other = (Node)o;
            WorldPoint this$position = this.getPosition();
            WorldPoint other$position = other.getPosition();
            if (this$position == null ? other$position != null : !this$position.equals(other$position)) {
                return false;
            }
            Node this$previous = this.getPrevious();
            Node other$previous = other.getPrevious();
            return !(this$previous == null ? other$previous != null : !((Object)this$previous).equals(other$previous));
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            WorldPoint $position = this.getPosition();
            result = result * 59 + ($position == null ? 43 : $position.hashCode());
            Node $previous = this.getPrevious();
            result = result * 59 + ($previous == null ? 43 : ((Object)$previous).hashCode());
            return result;
        }

        public String toString() {
            return "Pathfinder.Node(position=" + String.valueOf(this.getPosition()) + ", previous=" + String.valueOf(this.getPrevious()) + ")";
        }
    }
}

