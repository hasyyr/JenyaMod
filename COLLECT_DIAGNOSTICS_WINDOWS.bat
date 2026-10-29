@echo off
chcp 65001 >nul
cd /d "%~dp0"
python tools\validate_project.py
python tools\audit_api_surface.py
python tools\check_build_prerequisites.py
python tools\collect_diagnostics.py
pause
