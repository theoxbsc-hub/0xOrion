@echo off
title 0x Account Manager
rem Manage accounts (username/password + proxy + unique MAC identity) BEFORE launching.
rem Same idea as Orion x64 Add-Account.bat / account-manager.ps1.
rem Close the client before editing if it is running.
powershell -STA -ExecutionPolicy Bypass -File "%~dp0tools\account-manager\OxAccountManager.ps1"
