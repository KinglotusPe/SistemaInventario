@echo off
chcp 65001 > nul
title Sistema de Inventario - Probador de APIs REST y Registro JSON
cls
echo =========================================================================
echo   SISTEMA DE INVENTARIO - PRUEBA EN VIVO DE ENDPOINTS JSON Y RBAC
echo =========================================================================
echo.
node "%~dp0probar_apis.js"
echo.
pause
