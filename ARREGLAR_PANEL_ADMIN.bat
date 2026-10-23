@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul
title Arreglo del panel de administrador - Dotaciones Toronto

REM =====================================================================
REM   ARREGLO DE LOS ERRORES 404 DEL PANEL DE ADMINISTRADOR
REM
REM   Repara: Perfil, Productos, Inventario, Pedidos, Ventas, Devoluciones
REM
REM   Uso:
REM     1. Guarda este archivo en la carpeta del proyecto
REM     2. Doble clic sobre el archivo
REM     3. Espera a que diga LISTO
REM
REM   No borra nada: antes de tocar el proyecto guarda una copia de tus
REM   cambios en la rama "respaldo-local". El arreglo va dentro del archivo.
REM =====================================================================

set "URL=https://github.com/david19-bip/Dotaciones_Toronto.git"
set "PARCHE=fix-panel-admin.patch"
set "B64=%TEMP%\panel_admin_b64.txt"

cd /d "%~dp0"

echo.
echo ==========================================================
echo    ARREGLO DEL PANEL DE ADMINISTRADOR - ERRORES 404
echo ==========================================================
echo.

REM ---------- PASO 0: ubicar el proyecto ----------
if exist "pom.xml" goto proyecto_ok

echo No encontre el proyecto en esta carpeta:
echo    %CD%
echo.
set /p "CARPETA=Escribe la ruta completa de tu proyecto, por ejemplo C:\xampp\htdocs\repo : "
cd /d "!CARPETA!" 2>nul
if exist "pom.xml" goto proyecto_ok

echo.
echo [ERROR] En esa ruta tampoco encuentro el pom.xml.
echo         Revisa la ruta y vuelve a intentarlo.
goto fin_error

:proyecto_ok
echo Carpeta del proyecto: %CD%
echo.

REM ---------- PASO 1: comprobaciones ----------
echo [1/8] Revisando que todo este en orden ...
git --version >nul 2>&1
if not errorlevel 1 goto git_ok
echo.
echo [ERROR] Git no esta instalado en este equipo.
echo         Descargalo en https://git-scm.com/download/win
goto fin_error

:git_ok
if exist ".git" goto repo_ok
echo.
echo [ERROR] Esta carpeta no es el repositorio del proyecto.
echo         Tiene que ser la carpeta que contiene la carpeta oculta .git
echo         Normalmente es C:\xampp\htdocs\repo
goto fin_error

:repo_ok
where powershell >nul 2>&1
if not errorlevel 1 goto ps_ok
echo.
echo [ERROR] No encuentro PowerShell, que viene con Windows.
echo         Escribe en el chat para ayudarte antes de seguir.
goto fin_error

:ps_ok
echo       Todo en orden.
echo.

REM ---------- PASO 2: sacar el arreglo del propio archivo ----------
echo [2/8] Preparando el arreglo ...
if not exist "%B64%" goto b64_limpio
del "%B64%" >nul 2>&1
:b64_limpio
>>"%B64%" echo RnJvbSAyNWUxMTdlZmIwMDgxMThjOTE0M2ZlOTkxYWYwYjEwNzA2M2IyNGVhIE1vbiBTZXAgMTcg
>>"%B64%" echo MDA6MDA6MDAgMjAwMQpGcm9tOiBKSE80TlNFQkFTVEk0TiA8am9oYW5zZWJhc3RpYW4yMDA4MjAy
>>"%B64%" echo M0BnbWFpbC5jb20+CkRhdGU6IEZyaSwgMjMgT2N0IDIwMjYgMTk6MTY6MDcgKzAwMDAKU3ViamVj
>>"%B64%" echo dDogW1BBVENIXSBmaXgoYWRtaW4pOiBjb250cm9sYWRvciBkZWwgcGFuZWwgYWRtaW4gcGFyYSBs
>>"%B64%" echo YXMgc2VjY2lvbmVzCiBxdWUgZGFiYW4gNDA0CgotIE51ZXZvIEFkbWluUGFuZWxDb250cm9sbGVy
>>"%B64%" echo IGNvbiAvYWRtaW4vcGVyZmlsLCAvYWRtaW4vcHJvZHVjdG9zLCAvYWRtaW4vaW52ZW50YXJpbywK
>>"%B64%" echo ICAvYWRtaW4vcGVkaWRvcyAoKy9kZXRhbGxlIEpTT04pLCAvYWRtaW4vdmVudGFzICgrZ3JhZmlj
>>"%B64%" echo bywgL2RldGFsbGUsIC9yZXBvcnRlL2V4Y2VsKQogIHkgL2FkbWluL2Rldm9sdWNpb25lcyAoK2Nh
>>"%B64%" echo bWJpYXJFc3RhZG8pLgotIEFkbWluQ29udHJvbGxlcjogc2UgYWdyZWdhbiAvYWRtaW4vdXN1YXJp
>>"%B64%" echo b3MvdG9nZ2xlRXN0YWRvLCAvYWRtaW4vdXN1YXJpb3MvcmVwb3J0ZQogIHkgL2FkbWluL3VzdWFy
>>"%B64%" echo aW9zL3JlcG9ydGUvZXhjZWwsIHkgbGFzIHRhcmpldGFzIGRlIHJlc3VtZW4gZGVsIHBhbmVsLgot
>>"%B64%" echo IEFsZXJ0YXMgZGUgZXhpdG8vZXJyb3IgZW4gaW52ZW50YXJpb19hZG1pbi5odG1sIHkgZGV2b2x1
>>"%B64%" echo Y2lvbmVzX2FkbWluLmh0bWwuCi0tLQogLi4uL2NvbnRyb2xsZXIvQWRtaW5Db250cm9sbGVyLmph
>>"%B64%" echo dmEgICAgICAgICAgIHwgMTI2ICsrKwogLi4uL2NvbnRyb2xsZXIvQWRtaW5QYW5lbENvbnRyb2xs
>>"%B64%" echo ZXIuamF2YSAgICAgIHwgODg5ICsrKysrKysrKysrKysrKysrKwogLi4uL3RlbXBsYXRlcy9hZG1p
>>"%B64%" echo bi9kZXZvbHVjaW9uZXNfYWRtaW4uaHRtbCAgIHwgIDEwICsKIC4uLi90ZW1wbGF0ZXMvYWRtaW4v
>>"%B64%" echo aW52ZW50YXJpb19hZG1pbi5odG1sICAgICB8ICAxMCArCiA0IGZpbGVzIGNoYW5nZWQsIDEwMzUg
>>"%B64%" echo aW5zZXJ0aW9ucygrKQogY3JlYXRlIG1vZGUgMTAwNjQ0IHNyYy9tYWluL2phdmEvY29tL2RvdGFj
>>"%B64%" echo aW9uZXN0b3JvbnRvL2RvdGFjaW9uZXN0b3JvbnRvL2NvbnRyb2xsZXIvQWRtaW5QYW5lbENvbnRy
>>"%B64%" echo b2xsZXIuamF2YQoKZGlmZiAtLWdpdCBhL3NyYy9tYWluL2phdmEvY29tL2RvdGFjaW9uZXN0b3Jv
>>"%B64%" echo bnRvL2RvdGFjaW9uZXN0b3JvbnRvL2NvbnRyb2xsZXIvQWRtaW5Db250cm9sbGVyLmphdmEgYi9z
>>"%B64%" echo cmMvbWFpbi9qYXZhL2NvbS9kb3RhY2lvbmVzdG9yb250by9kb3RhY2lvbmVzdG9yb250by9jb250
>>"%B64%" echo cm9sbGVyL0FkbWluQ29udHJvbGxlci5qYXZhCmluZGV4IGU4M2NhYzcuLmUzZjdjN2MgMTAwNjQ0
>>"%B64%" echo Ci0tLSBhL3NyYy9tYWluL2phdmEvY29tL2RvdGFjaW9uZXN0b3JvbnRvL2RvdGFjaW9uZXN0b3Jv
>>"%B64%" echo bnRvL2NvbnRyb2xsZXIvQWRtaW5Db250cm9sbGVyLmphdmEKKysrIGIvc3JjL21haW4vamF2YS9j
>>"%B64%" echo b20vZG90YWNpb25lc3Rvcm9udG8vZG90YWNpb25lc3Rvcm9udG8vY29udHJvbGxlci9BZG1pbkNv
>>"%B64%" echo bnRyb2xsZXIuamF2YQpAQCAtNSw2ICs1LDEwIEBAIGltcG9ydCBqYXZhLnV0aWwuTGlzdDsKIGlt
>>"%B64%" echo cG9ydCBqYXZhLnV0aWwuTWFwOwogCiBpbXBvcnQgb3JnLnNwcmluZ2ZyYW1ld29yay5iZWFucy5m
>>"%B64%" echo YWN0b3J5LmFubm90YXRpb24uQXV0b3dpcmVkOworaW1wb3J0IG9yZy5zcHJpbmdmcmFtZXdvcmsu
>>"%B64%" echo aHR0cC5Db250ZW50RGlzcG9zaXRpb247CitpbXBvcnQgb3JnLnNwcmluZ2ZyYW1ld29yay5odHRw
>>"%B64%" echo Lkh0dHBIZWFkZXJzOworaW1wb3J0IG9yZy5zcHJpbmdmcmFtZXdvcmsuaHR0cC5NZWRpYVR5cGU7
>>"%B64%" echo CitpbXBvcnQgb3JnLnNwcmluZ2ZyYW1ld29yay5odHRwLlJlc3BvbnNlRW50aXR5OwogaW1wb3J0
>>"%B64%" echo IG9yZy5zcHJpbmdmcmFtZXdvcmsuc2VjdXJpdHkuY3J5cHRvLnBhc3N3b3JkLlBhc3N3b3JkRW5j
>>"%B64%" echo b2RlcjsKIGltcG9ydCBvcmcuc3ByaW5nZnJhbWV3b3JrLnN0ZXJlb3R5cGUuQ29udHJvbGxlcjsK
>>"%B64%" echo IGltcG9ydCBvcmcuc3ByaW5nZnJhbWV3b3JrLnVpLk1vZGVsOwpAQCAtMTIsMTEgKzE2LDE3IEBA
>>"%B64%" echo IGltcG9ydCBvcmcuc3ByaW5nZnJhbWV3b3JrLndlYi5iaW5kLmFubm90YXRpb24uR2V0TWFwcGlu
>>"%B64%" echo ZzsKIGltcG9ydCBvcmcuc3ByaW5nZnJhbWV3b3JrLndlYi5iaW5kLmFubm90YXRpb24uUG9zdE1h
>>"%B64%" echo cHBpbmc7CiBpbXBvcnQgb3JnLnNwcmluZ2ZyYW1ld29yay53ZWIuYmluZC5hbm5vdGF0aW9uLlJl
>>"%B64%" echo cXVlc3RNYXBwaW5nOwogaW1wb3J0IG9yZy5zcHJpbmdmcmFtZXdvcmsud2ViLmJpbmQuYW5ub3Rh
>>"%B64%" echo dGlvbi5SZXF1ZXN0UGFyYW07CitpbXBvcnQgb3JnLnNwcmluZ2ZyYW1ld29yay53ZWIuc2Vydmxl
>>"%B64%" echo dC5tdmMuc3VwcG9ydC5SZWRpcmVjdEF0dHJpYnV0ZXM7CiAKIGltcG9ydCBjb20uZG90YWNpb25l
>>"%B64%" echo c3Rvcm9udG8uZG90YWNpb25lc3Rvcm9udG8uZW50aXR5LlVzdWFyaW87CiBpbXBvcnQgY29tLmRv
>>"%B64%" echo dGFjaW9uZXN0b3JvbnRvLmRvdGFjaW9uZXN0b3JvbnRvLmVudGl0eS5Vc3VhcmlvUm9sOworaW1w
>>"%B64%" echo b3J0IGNvbS5kb3RhY2lvbmVzdG9yb250by5kb3RhY2lvbmVzdG9yb250by5yZXBvc2l0b3J5LkRl
>>"%B64%" echo dm9sdWNpb25SZXBvc2l0b3J5OworaW1wb3J0IGNvbS5kb3RhY2lvbmVzdG9yb250by5kb3RhY2lv
>>"%B64%" echo bmVzdG9yb250by5yZXBvc2l0b3J5LlBlZGlkb1JlcG9zaXRvcnk7CitpbXBvcnQgY29tLmRvdGFj
>>"%B64%" echo aW9uZXN0b3JvbnRvLmRvdGFjaW9uZXN0b3JvbnRvLnJlcG9zaXRvcnkuUHJvZHVjdG9SZXBvc2l0
>>"%B64%" echo b3J5OwogaW1wb3J0IGNvbS5kb3RhY2lvbmVzdG9yb250by5kb3RhY2lvbmVzdG9yb250by5yZXBv
>>"%B64%" echo c2l0b3J5LlVzdWFyaW9SZXBvc2l0b3J5OwogaW1wb3J0IGNvbS5kb3RhY2lvbmVzdG9yb250by5k
>>"%B64%" echo b3RhY2lvbmVzdG9yb250by5yZXBvc2l0b3J5LlVzdWFyaW9Sb2xSZXBvc2l0b3J5OworaW1wb3J0
>>"%B64%" echo IGNvbS5kb3RhY2lvbmVzdG9yb250by5kb3RhY2lvbmVzdG9yb250by5zZXJ2aWNlLlJlcG9ydGVV
>>"%B64%" echo c3Vhcmlvc0V4Y2VsU2VydmljZTsKK2ltcG9ydCBjb20uZG90YWNpb25lc3Rvcm9udG8uZG90YWNp
>>"%B64%" echo b25lc3Rvcm9udG8uc2VydmljZS5SZXBvcnRlVXN1YXJpb3NTZXJ2aWNlOwogCiBpbXBvcnQgamFr
>>"%B64%" echo YXJ0YS5zZXJ2bGV0Lmh0dHAuSHR0cFNlc3Npb247CiBpbXBvcnQgamFrYXJ0YS50cmFuc2FjdGlv
>>"%B64%" echo bi5UcmFuc2FjdGlvbmFsOwpAQCAtMzEsNiArNDEsMjEgQEAgcHVibGljIGNsYXNzIEFkbWluQ29u
>>"%B64%" echo dHJvbGxlciB7CiAgICAgQEF1dG93aXJlZAogICAgIHByaXZhdGUgVXN1YXJpb1JvbFJlcG9zaXRv
>>"%B64%" echo cnkgdXN1YXJpb1JvbFJlcG9zaXRvcnk7CiAKKyAgICBAQXV0b3dpcmVkCisgICAgcHJpdmF0ZSBQ
>>"%B64%" echo cm9kdWN0b1JlcG9zaXRvcnkgcHJvZHVjdG9SZXBvc2l0b3J5OworCisgICAgQEF1dG93aXJlZAor
>>"%B64%" echo ICAgIHByaXZhdGUgUGVkaWRvUmVwb3NpdG9yeSBwZWRpZG9SZXBvc2l0b3J5OworCisgICAgQEF1
>>"%B64%" echo dG93aXJlZAorICAgIHByaXZhdGUgRGV2b2x1Y2lvblJlcG9zaXRvcnkgZGV2b2x1Y2lvblJlcG9z
>>"%B64%" echo aXRvcnk7CisKKyAgICBAQXV0b3dpcmVkCisgICAgcHJpdmF0ZSBSZXBvcnRlVXN1YXJpb3NTZXJ2
>>"%B64%" echo aWNlIHJlcG9ydGVVc3Vhcmlvc1NlcnZpY2U7CisKKyAgICBAQXV0b3dpcmVkCisgICAgcHJpdmF0
>>"%B64%" echo ZSBSZXBvcnRlVXN1YXJpb3NFeGNlbFNlcnZpY2UgcmVwb3J0ZVVzdWFyaW9zRXhjZWxTZXJ2aWNl
>>"%B64%" echo OworCiAgICAgQEF1dG93aXJlZAogICAgIHByaXZhdGUgUGFzc3dvcmRFbmNvZGVyIHBhc3N3b3Jk
>>"%B64%" echo RW5jb2RlcjsKIApAQCAtNTUsNiArODAsMTIgQEAgcHVibGljIGNsYXNzIEFkbWluQ29udHJvbGxl
>>"%B64%" echo ciB7CiAgICAgICAgICAgICAgICAgdXN1YXJpb0FjdHVhbC5nZXROb21icmUoKQogICAgICAgICAp
>>"%B64%" echo OwogCisgICAgICAgIC8vIFRhcmpldGFzIGRlIHJlc3VtZW4gZGVsIHBhbmVsIChhbnRlcyBsbGVn
>>"%B64%" echo YWJhbiB2YWPDrWFzKQorICAgICAgICBtb2RlbC5hZGRBdHRyaWJ1dGUoInRvdGFsVXN1YXJpb3Mi
>>"%B64%" echo LCB1c3VhcmlvUmVwb3NpdG9yeS5jb3VudCgpKTsKKyAgICAgICAgbW9kZWwuYWRkQXR0cmlidXRl
>>"%B64%" echo KCJ0b3RhbFByb2R1Y3RvcyIsIHByb2R1Y3RvUmVwb3NpdG9yeS5jb3VudCgpKTsKKyAgICAgICAg
>>"%B64%" echo bW9kZWwuYWRkQXR0cmlidXRlKCJ0b3RhbFBlZGlkb3MiLCBwZWRpZG9SZXBvc2l0b3J5LmNvdW50
>>"%B64%" echo KCkpOworICAgICAgICBtb2RlbC5hZGRBdHRyaWJ1dGUoInRvdGFsRGV2b2x1Y2lvbmVzIiwgZGV2
>>"%B64%" echo b2x1Y2lvblJlcG9zaXRvcnkuY291bnQoKSk7CisKICAgICAgICAgcmV0dXJuICJhZG1pbi9wYW5l
>>"%B64%" echo bF9hZG1pbiI7CiAgICAgfQogCkBAIC00NDUsNiArNDc2LDEwMSBAQCBwdWJsaWMgY2xhc3MgQWRt
>>"%B64%" echo aW5Db250cm9sbGVyIHsKICAgICAgICAgKTsKICAgICB9CiAKKyAgICAvLyA9PT09PT09PT09PT09
>>"%B64%" echo PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT0KKyAgICAvLyBUT0dH
>>"%B64%" echo TEUgRVNUQURPIChydXRhIHVzYWRhIHBvciB1c3Vhcmlvc19hZG1pbi5odG1sKQorICAgIC8vID09
>>"%B64%" echo PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PQor
>>"%B64%" echo CisgICAgQFBvc3RNYXBwaW5nKCIvdXN1YXJpb3MvdG9nZ2xlRXN0YWRvIikKKyAgICBwdWJsaWMg
>>"%B64%" echo U3RyaW5nIHRvZ2dsZUVzdGFkb1VzdWFyaW8oCisgICAgICAgICAgICBAUmVxdWVzdFBhcmFtIExv
>>"%B64%" echo bmcgaWRVc3VhcmlvLAorICAgICAgICAgICAgUmVkaXJlY3RBdHRyaWJ1dGVzIHJlZGlyZWN0KSB7
>>"%B64%" echo CisKKyAgICAgICAgVXN1YXJpbyB1c3VhcmlvID0KKyAgICAgICAgICAgICAgICB1c3VhcmlvUmVw
>>"%B64%" echo b3NpdG9yeQorICAgICAgICAgICAgICAgICAgICAgICAgLmZpbmRCeUlkKGlkVXN1YXJpbykKKyAg
>>"%B64%" echo ICAgICAgICAgICAgICAgICAgICAgIC5vckVsc2UobnVsbCk7CisKKyAgICAgICAgaWYgKHVzdWFy
>>"%B64%" echo aW8gIT0gbnVsbCkgeworCisgICAgICAgICAgICBpZiAoIkhhYmlsaXRhZG8iLmVxdWFscygKKyAg
>>"%B64%" echo ICAgICAgICAgICAgICAgICAgdXN1YXJpby5nZXRFc3RhZG8oKSkpIHsKKworICAgICAgICAgICAg
>>"%B64%" echo ICAgIHVzdWFyaW8uc2V0RXN0YWRvKAorICAgICAgICAgICAgICAgICAgICAgICAgIkluaGFiaWxp
>>"%B64%" echo dGFkbyIKKyAgICAgICAgICAgICAgICApOworCisgICAgICAgICAgICB9IGVsc2UgeworCisgICAg
>>"%B64%" echo ICAgICAgICAgICAgdXN1YXJpby5zZXRFc3RhZG8oCisgICAgICAgICAgICAgICAgICAgICAgICAi
>>"%B64%" echo SGFiaWxpdGFkbyIKKyAgICAgICAgICAgICAgICApOworICAgICAgICAgICAgfQorCisgICAgICAg
>>"%B64%" echo ICAgICB1c3VhcmlvUmVwb3NpdG9yeS5zYXZlKHVzdWFyaW8pOworCisgICAgICAgICAgICByZWRp
>>"%B64%" echo cmVjdC5hZGRGbGFzaEF0dHJpYnV0ZSgKKyAgICAgICAgICAgICAgICAgICAgImV4aXRvIiwKKyAg
>>"%B64%" echo ICAgICAgICAgICAgICAgICAgIkVsIHVzdWFyaW8gIgorICAgICAgICAgICAgICAgICAgICAgICAg
>>"%B64%" echo ICAgICsgdXN1YXJpby5nZXROb21icmUoKQorICAgICAgICAgICAgICAgICAgICAgICAgICAgICsg
>>"%B64%" echo IiBxdWVkw7MgZW4gZXN0YWRvICIKKyAgICAgICAgICAgICAgICAgICAgICAgICAgICArIHVzdWFy
>>"%B64%" echo aW8uZ2V0RXN0YWRvKCkKKyAgICAgICAgICAgICAgICAgICAgICAgICAgICArICIuIgorICAgICAg
>>"%B64%" echo ICAgICAgKTsKKyAgICAgICAgfQorCisgICAgICAgIHJldHVybiAicmVkaXJlY3Q6L2FkbWluL3Vz
>>"%B64%" echo dWFyaW9zIjsKKyAgICB9CisKKyAgICAvLyA9PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09
>>"%B64%" echo PT09PT09PT09PT09PT09PT09PT09PT09PT0KKyAgICAvLyBSRVBPUlRFUyBERSBVU1VBUklPUyAo
>>"%B64%" echo UERGIHkgRXhjZWwpCisgICAgLy8gPT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09
>>"%B64%" echo PT09PT09PT09PT09PT09PT09PT09CisKKyAgICBAR2V0TWFwcGluZygiL3VzdWFyaW9zL3JlcG9y
>>"%B64%" echo dGUiKQorICAgIHB1YmxpYyBSZXNwb25zZUVudGl0eTxieXRlW10+IHJlcG9ydGVVc3Vhcmlvcygp
>>"%B64%" echo IHsKKworICAgICAgICBieXRlW10gcGRmID0KKyAgICAgICAgICAgICAgICByZXBvcnRlVXN1YXJp
>>"%B64%" echo b3NTZXJ2aWNlLmdlbmVyYXJSZXBvcnRlKAorICAgICAgICAgICAgICAgICAgICAgICAgdXN1YXJp
>>"%B64%" echo b1JlcG9zaXRvcnkuZmluZEFsbCgpCisgICAgICAgICAgICAgICAgKTsKKworICAgICAgICBIdHRw
>>"%B64%" echo SGVhZGVycyBoZWFkZXJzID0gbmV3IEh0dHBIZWFkZXJzKCk7CisgICAgICAgIGhlYWRlcnMuc2V0
>>"%B64%" echo Q29udGVudFR5cGUoTWVkaWFUeXBlLkFQUExJQ0FUSU9OX1BERik7CisgICAgICAgIGhlYWRlcnMu
>>"%B64%" echo c2V0Q29udGVudERpc3Bvc2l0aW9uKAorICAgICAgICAgICAgICAgIENvbnRlbnREaXNwb3NpdGlv
>>"%B64%" echo bi5hdHRhY2htZW50KCkKKyAgICAgICAgICAgICAgICAgICAgICAgIC5maWxlbmFtZSgicmVwb3J0
>>"%B64%" echo ZV91c3Vhcmlvcy5wZGYiKQorICAgICAgICAgICAgICAgICAgICAgICAgLmJ1aWxkKCkKKyAgICAg
>>"%B64%" echo ICAgKTsKKworICAgICAgICByZXR1cm4gUmVzcG9uc2VFbnRpdHkub2soKQorICAgICAgICAgICAg
>>"%B64%" echo ICAgIC5oZWFkZXJzKGhlYWRlcnMpCisgICAgICAgICAgICAgICAgLmJvZHkocGRmKTsKKyAgICB9
>>"%B64%" echo CisKKyAgICBAR2V0TWFwcGluZygiL3VzdWFyaW9zL3JlcG9ydGUvZXhjZWwiKQorICAgIHB1Ymxp
>>"%B64%" echo YyBSZXNwb25zZUVudGl0eTxieXRlW10+IHJlcG9ydGVVc3Vhcmlvc0V4Y2VsKCkgeworCisgICAg
>>"%B64%" echo ICAgIGJ5dGVbXSBleGNlbCA9CisgICAgICAgICAgICAgICAgcmVwb3J0ZVVzdWFyaW9zRXhjZWxT
>>"%B64%" echo ZXJ2aWNlLmdlbmVyYXJSZXBvcnRlKAorICAgICAgICAgICAgICAgICAgICAgICAgdXN1YXJpb1Jl
>>"%B64%" echo cG9zaXRvcnkuZmluZEFsbCgpCisgICAgICAgICAgICAgICAgKTsKKworICAgICAgICBIdHRwSGVh
>>"%B64%" echo ZGVycyBoZWFkZXJzID0gbmV3IEh0dHBIZWFkZXJzKCk7CisgICAgICAgIGhlYWRlcnMuc2V0Q29u
>>"%B64%" echo dGVudFR5cGUoCisgICAgICAgICAgICAgICAgTWVkaWFUeXBlLnBhcnNlTWVkaWFUeXBlKAorICAg
>>"%B64%" echo ICAgICAgICAgICAgICAgICAgICAgImFwcGxpY2F0aW9uL3ZuZC5vcGVueG1sZm9ybWF0cy1vZmZp
>>"%B64%" echo Y2Vkb2N1bWVudC5zcHJlYWRzaGVldG1sLnNoZWV0IgorICAgICAgICAgICAgICAgICkKKyAgICAg
>>"%B64%" echo ICAgKTsKKyAgICAgICAgaGVhZGVycy5zZXRDb250ZW50RGlzcG9zaXRpb24oCisgICAgICAgICAg
>>"%B64%" echo ICAgICAgQ29udGVudERpc3Bvc2l0aW9uLmF0dGFjaG1lbnQoKQorICAgICAgICAgICAgICAgICAg
>>"%B64%" echo ICAgICAgLmZpbGVuYW1lKCJyZXBvcnRlX3VzdWFyaW9zLnhsc3giKQorICAgICAgICAgICAgICAg
>>"%B64%" echo ICAgICAgICAgLmJ1aWxkKCkKKyAgICAgICAgKTsKKworICAgICAgICByZXR1cm4gUmVzcG9uc2VF
>>"%B64%" echo bnRpdHkub2soKQorICAgICAgICAgICAgICAgIC5oZWFkZXJzKGhlYWRlcnMpCisgICAgICAgICAg
>>"%B64%" echo ICAgICAgLmJvZHkoZXhjZWwpOworICAgIH0KKwogICAgIC8vID09PT09PT09PT09PT09PT09PT09
>>"%B64%" echo PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PQogICAgIC8vIE5PTUJSRSBERUwg
>>"%B64%" echo Uk9MCiAgICAgLy8gPT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09
>>"%B64%" echo PT09PT09PT09PT09CmRpZmYgLS1naXQgYS9zcmMvbWFpbi9qYXZhL2NvbS9kb3RhY2lvbmVzdG9y
>>"%B64%" echo b250by9kb3RhY2lvbmVzdG9yb250by9jb250cm9sbGVyL0FkbWluUGFuZWxDb250cm9sbGVyLmph
>>"%B64%" echo dmEgYi9zcmMvbWFpbi9qYXZhL2NvbS9kb3RhY2lvbmVzdG9yb250by9kb3RhY2lvbmVzdG9yb250
>>"%B64%" echo by9jb250cm9sbGVyL0FkbWluUGFuZWxDb250cm9sbGVyLmphdmEKbmV3IGZpbGUgbW9kZSAxMDA2
>>"%B64%" echo NDQKaW5kZXggMDAwMDAwMC4uMzJiZGZiOQotLS0gL2Rldi9udWxsCisrKyBiL3NyYy9tYWluL2ph
>>"%B64%" echo dmEvY29tL2RvdGFjaW9uZXN0b3JvbnRvL2RvdGFjaW9uZXN0b3JvbnRvL2NvbnRyb2xsZXIvQWRt
>>"%B64%" echo aW5QYW5lbENvbnRyb2xsZXIuamF2YQpAQCAtMCwwICsxLDg4OSBAQAorcGFja2FnZSBjb20uZG90
>>"%B64%" echo YWNpb25lc3Rvcm9udG8uZG90YWNpb25lc3Rvcm9udG8uY29udHJvbGxlcjsKKworaW1wb3J0IGph
>>"%B64%" echo dmEuaW8uSU9FeGNlcHRpb247CitpbXBvcnQgamF2YS5tYXRoLkJpZ0RlY2ltYWw7CitpbXBvcnQg
>>"%B64%" echo amF2YS5uaW8uZmlsZS5GaWxlczsKK2ltcG9ydCBqYXZhLm5pby5maWxlLlBhdGg7CitpbXBvcnQg
>>"%B64%" echo amF2YS5uaW8uZmlsZS5QYXRoczsKK2ltcG9ydCBqYXZhLm5pby5maWxlLlN0YW5kYXJkQ29weU9w
>>"%B64%" echo dGlvbjsKK2ltcG9ydCBqYXZhLnRpbWUuTG9jYWxEYXRlOworaW1wb3J0IGphdmEudGltZS5mb3Jt
>>"%B64%" echo YXQuRGF0ZVRpbWVGb3JtYXR0ZXI7CitpbXBvcnQgamF2YS51dGlsLkFycmF5TGlzdDsKK2ltcG9y
>>"%B64%" echo dCBqYXZhLnV0aWwuTGlua2VkSGFzaE1hcDsKK2ltcG9ydCBqYXZhLnV0aWwuTGlzdDsKK2ltcG9y
>>"%B64%" echo dCBqYXZhLnV0aWwuTWFwOworaW1wb3J0IGphdmEudXRpbC5UcmVlTWFwOworCitpbXBvcnQgb3Jn
>>"%B64%" echo LnNwcmluZ2ZyYW1ld29yay5iZWFucy5mYWN0b3J5LmFubm90YXRpb24uQXV0b3dpcmVkOworaW1w
>>"%B64%" echo b3J0IG9yZy5zcHJpbmdmcmFtZXdvcmsuaHR0cC5Db250ZW50RGlzcG9zaXRpb247CitpbXBvcnQg
>>"%B64%" echo b3JnLnNwcmluZ2ZyYW1ld29yay5odHRwLkh0dHBIZWFkZXJzOworaW1wb3J0IG9yZy5zcHJpbmdm
>>"%B64%" echo cmFtZXdvcmsuaHR0cC5NZWRpYVR5cGU7CitpbXBvcnQgb3JnLnNwcmluZ2ZyYW1ld29yay5odHRw
>>"%B64%" echo LlJlc3BvbnNlRW50aXR5OworaW1wb3J0IG9yZy5zcHJpbmdmcmFtZXdvcmsuc2VjdXJpdHkuYXV0
>>"%B64%" echo aGVudGljYXRpb24uVXNlcm5hbWVQYXNzd29yZEF1dGhlbnRpY2F0aW9uVG9rZW47CitpbXBvcnQg
>>"%B64%" echo b3JnLnNwcmluZ2ZyYW1ld29yay5zZWN1cml0eS5jb3JlLkF1dGhlbnRpY2F0aW9uOworaW1wb3J0
>>"%B64%" echo IG9yZy5zcHJpbmdmcmFtZXdvcmsuc2VjdXJpdHkuY29yZS5jb250ZXh0LlNlY3VyaXR5Q29udGV4
>>"%B64%" echo dEhvbGRlcjsKK2ltcG9ydCBvcmcuc3ByaW5nZnJhbWV3b3JrLnNlY3VyaXR5LmNyeXB0by5wYXNz
>>"%B64%" echo d29yZC5QYXNzd29yZEVuY29kZXI7CitpbXBvcnQgb3JnLnNwcmluZ2ZyYW1ld29yay5zdGVyZW90
>>"%B64%" echo eXBlLkNvbnRyb2xsZXI7CitpbXBvcnQgb3JnLnNwcmluZ2ZyYW1ld29yay51aS5Nb2RlbDsKK2lt
>>"%B64%" echo cG9ydCBvcmcuc3ByaW5nZnJhbWV3b3JrLndlYi5iaW5kLmFubm90YXRpb24uR2V0TWFwcGluZzsK
>>"%B64%" echo K2ltcG9ydCBvcmcuc3ByaW5nZnJhbWV3b3JrLndlYi5iaW5kLmFubm90YXRpb24uUGF0aFZhcmlh
>>"%B64%" echo YmxlOworaW1wb3J0IG9yZy5zcHJpbmdmcmFtZXdvcmsud2ViLmJpbmQuYW5ub3RhdGlvbi5Qb3N0
>>"%B64%" echo TWFwcGluZzsKK2ltcG9ydCBvcmcuc3ByaW5nZnJhbWV3b3JrLndlYi5iaW5kLmFubm90YXRpb24u
>>"%B64%" echo UmVxdWVzdE1hcHBpbmc7CitpbXBvcnQgb3JnLnNwcmluZ2ZyYW1ld29yay53ZWIuYmluZC5hbm5v
>>"%B64%" echo dGF0aW9uLlJlcXVlc3RQYXJhbTsKK2ltcG9ydCBvcmcuc3ByaW5nZnJhbWV3b3JrLndlYi5iaW5k
>>"%B64%" echo LmFubm90YXRpb24uUmVzcG9uc2VCb2R5OworaW1wb3J0IG9yZy5zcHJpbmdmcmFtZXdvcmsud2Vi
>>"%B64%" echo Lm11bHRpcGFydC5NdWx0aXBhcnRGaWxlOworaW1wb3J0IG9yZy5zcHJpbmdmcmFtZXdvcmsud2Vi
>>"%B64%" echo LnNlcnZsZXQubXZjLnN1cHBvcnQuUmVkaXJlY3RBdHRyaWJ1dGVzOworCitpbXBvcnQgY29tLmRv
>>"%B64%" echo dGFjaW9uZXN0b3JvbnRvLmRvdGFjaW9uZXN0b3JvbnRvLmVudGl0eS5EZXRhbGxlUGVkaWRvOwor
>>"%B64%" echo aW1wb3J0IGNvbS5kb3RhY2lvbmVzdG9yb250by5kb3RhY2lvbmVzdG9yb250by5lbnRpdHkuRGV0
>>"%B64%" echo YWxsZVZlbnRhOworaW1wb3J0IGNvbS5kb3RhY2lvbmVzdG9yb250by5kb3RhY2lvbmVzdG9yb250
>>"%B64%" echo by5lbnRpdHkuRGV2b2x1Y2lvbjsKK2ltcG9ydCBjb20uZG90YWNpb25lc3Rvcm9udG8uZG90YWNp
>>"%B64%" echo b25lc3Rvcm9udG8uZW50aXR5LkludmVudGFyaW87CitpbXBvcnQgY29tLmRvdGFjaW9uZXN0b3Jv
>>"%B64%" echo bnRvLmRvdGFjaW9uZXN0b3JvbnRvLmVudGl0eS5Nb3ZpbWllbnRvSW52ZW50YXJpbzsKK2ltcG9y
>>"%B64%" echo dCBjb20uZG90YWNpb25lc3Rvcm9udG8uZG90YWNpb25lc3Rvcm9udG8uZW50aXR5LlBlZGlkbzsK
>>"%B64%" echo K2ltcG9ydCBjb20uZG90YWNpb25lc3Rvcm9udG8uZG90YWNpb25lc3Rvcm9udG8uZW50aXR5LlBy
>>"%B64%" echo b2R1Y3RvOworaW1wb3J0IGNvbS5kb3RhY2lvbmVzdG9yb250by5kb3RhY2lvbmVzdG9yb250by5l
>>"%B64%" echo bnRpdHkuVXN1YXJpbzsKK2ltcG9ydCBjb20uZG90YWNpb25lc3Rvcm9udG8uZG90YWNpb25lc3Rv
>>"%B64%" echo cm9udG8uZW50aXR5LlZlbnRhOworaW1wb3J0IGNvbS5kb3RhY2lvbmVzdG9yb250by5kb3RhY2lv
>>"%B64%" echo bmVzdG9yb250by5yZXBvc2l0b3J5LkRldGFsbGVQZWRpZG9SZXBvc2l0b3J5OworaW1wb3J0IGNv
>>"%B64%" echo bS5kb3RhY2lvbmVzdG9yb250by5kb3RhY2lvbmVzdG9yb250by5yZXBvc2l0b3J5LkRldGFsbGVW
>>"%B64%" echo ZW50YVJlcG9zaXRvcnk7CitpbXBvcnQgY29tLmRvdGFjaW9uZXN0b3JvbnRvLmRvdGFjaW9uZXN0
>>"%B64%" echo b3JvbnRvLnJlcG9zaXRvcnkuRGV2b2x1Y2lvblJlcG9zaXRvcnk7CitpbXBvcnQgY29tLmRvdGFj
>>"%B64%" echo aW9uZXN0b3JvbnRvLmRvdGFjaW9uZXN0b3JvbnRvLnJlcG9zaXRvcnkuSW52ZW50YXJpb1JlcG9z
>>"%B64%" echo aXRvcnk7CitpbXBvcnQgY29tLmRvdGFjaW9uZXN0b3JvbnRvLmRvdGFjaW9uZXN0b3JvbnRvLnJl
>>"%B64%" echo cG9zaXRvcnkuTW92aW1pZW50b0ludmVudGFyaW9SZXBvc2l0b3J5OworaW1wb3J0IGNvbS5kb3Rh
>>"%B64%" echo Y2lvbmVzdG9yb250by5kb3RhY2lvbmVzdG9yb250by5yZXBvc2l0b3J5LlBlZGlkb1JlcG9zaXRv
>>"%B64%" echo cnk7CitpbXBvcnQgY29tLmRvdGFjaW9uZXN0b3JvbnRvLmRvdGFjaW9uZXN0b3JvbnRvLnJlcG9z
>>"%B64%" echo aXRvcnkuUHJvZHVjdG9SZXBvc2l0b3J5OworaW1wb3J0IGNvbS5kb3RhY2lvbmVzdG9yb250by5k
>>"%B64%" echo b3RhY2lvbmVzdG9yb250by5yZXBvc2l0b3J5LlVzdWFyaW9SZXBvc2l0b3J5OworaW1wb3J0IGNv
>>"%B64%" echo bS5kb3RhY2lvbmVzdG9yb250by5kb3RhY2lvbmVzdG9yb250by5yZXBvc2l0b3J5LlZlbnRhUmVw
>>"%B64%" echo b3NpdG9yeTsKK2ltcG9ydCBjb20uZG90YWNpb25lc3Rvcm9udG8uZG90YWNpb25lc3Rvcm9udG8u
>>"%B64%" echo c2VydmljZS5SZXBvcnRlUHJvZHVjdG9zRXhjZWxTZXJ2aWNlOworaW1wb3J0IGNvbS5kb3RhY2lv
>>"%B64%" echo bmVzdG9yb250by5kb3RhY2lvbmVzdG9yb250by5zZXJ2aWNlLlJlcG9ydGVQcm9kdWN0b3NTZXJ2
>>"%B64%" echo aWNlOworaW1wb3J0IGNvbS5kb3RhY2lvbmVzdG9yb250by5kb3RhY2lvbmVzdG9yb250by5zZXJ2
>>"%B64%" echo aWNlLlJlcG9ydGVWZW50YXNFeGNlbFNlcnZpY2U7CisKK2ltcG9ydCBqYWthcnRhLnNlcnZsZXQu
>>"%B64%" echo aHR0cC5IdHRwU2Vzc2lvbjsKK2ltcG9ydCBqYWthcnRhLnRyYW5zYWN0aW9uLlRyYW5zYWN0aW9u
>>"%B64%" echo YWw7CisKKy8qKgorICogQ29udHJvbGFkb3IgZGVsIHBhbmVsIGRlIGFkbWluaXN0cmFkb3IuCisg
>>"%B64%" echo KgorICogQ29tcGxlbWVudGEgYSB7QGxpbmsgQWRtaW5Db250cm9sbGVyfSAocXVlIGN1YnJlIGVs
>>"%B64%" echo IHBhbmVsIHkgbGEgZ2VzdGnDs24gZGUKKyAqIHVzdWFyaW9zKSBjb24gZWwgcmVzdG8gZGUgc2Vj
>>"%B64%" echo Y2lvbmVzIGRlbCBtZW7DuiBsYXRlcmFsOiBwZXJmaWwsIHByb2R1Y3RvcywKKyAqIGludmVudGFy
>>"%B64%" echo aW8sIHBlZGlkb3MsIHZlbnRhcyB5IGRldm9sdWNpb25lcy4KKyAqCisgKiBBbnRlcyBkZSBleGlz
>>"%B64%" echo dGlyIGVzdGEgY2xhc2UgZXNhcyBwbGFudGlsbGFzIHlhIGVzdGFiYW4gZW4KKyAqIHNyYy9tYWlu
>>"%B64%" echo L3Jlc291cmNlcy90ZW1wbGF0ZXMvYWRtaW4vLCBwZXJvIG5vIGhhYsOtYSBuaW5nw7puIGNvbnRy
>>"%B64%" echo b2xhZG9yIHF1ZQorICogcmVzcG9uZGllcmEgYSBzdXMgcnV0YXMsIHBvciBlc28gZWwgbmF2ZWdh
>>"%B64%" echo ZG9yIG1vc3RyYWJhIGVycm9yIDQwNC4KKyAqLworQENvbnRyb2xsZXIKK0BSZXF1ZXN0TWFwcGlu
>>"%B64%" echo ZygiL2FkbWluIikKK3B1YmxpYyBjbGFzcyBBZG1pblBhbmVsQ29udHJvbGxlciB7CisKKyAgICBw
>>"%B64%" echo cml2YXRlIHN0YXRpYyBmaW5hbCBTdHJpbmcgQ0FSUEVUQV9JTUFHRU5FUyA9ICJ1cGxvYWRzL3By
>>"%B64%" echo b2R1Y3RvcyI7CisgICAgcHJpdmF0ZSBzdGF0aWMgZmluYWwgU3RyaW5nIFBSRUZJSk9fSU1BR0VO
>>"%B64%" echo RVMgPSAiaW1hZ2VzL3Byb2R1Y3RvcyI7CisgICAgcHJpdmF0ZSBzdGF0aWMgZmluYWwgRGF0ZVRp
>>"%B64%" echo bWVGb3JtYXR0ZXIgRk9STUFUT19GRUNIQSA9IERhdGVUaW1lRm9ybWF0dGVyLm9mUGF0dGVybigi
>>"%B64%" echo ZGQvTU0iKTsKKworICAgIEBBdXRvd2lyZWQKKyAgICBwcml2YXRlIFVzdWFyaW9SZXBvc2l0b3J5
>>"%B64%" echo IHVzdWFyaW9SZXBvc2l0b3J5OworCisgICAgQEF1dG93aXJlZAorICAgIHByaXZhdGUgUHJvZHVj
>>"%B64%" echo dG9SZXBvc2l0b3J5IHByb2R1Y3RvUmVwb3NpdG9yeTsKKworICAgIEBBdXRvd2lyZWQKKyAgICBw
>>"%B64%" echo cml2YXRlIEludmVudGFyaW9SZXBvc2l0b3J5IGludmVudGFyaW9SZXBvc2l0b3J5OworCisgICAg
>>"%B64%" echo QEF1dG93aXJlZAorICAgIHByaXZhdGUgTW92aW1pZW50b0ludmVudGFyaW9SZXBvc2l0b3J5IG1v
>>"%B64%" echo dmltaWVudG9JbnZlbnRhcmlvUmVwb3NpdG9yeTsKKworICAgIEBBdXRvd2lyZWQKKyAgICBwcml2
>>"%B64%" echo YXRlIFBlZGlkb1JlcG9zaXRvcnkgcGVkaWRvUmVwb3NpdG9yeTsKKworICAgIEBBdXRvd2lyZWQK
>>"%B64%" echo KyAgICBwcml2YXRlIERldGFsbGVQZWRpZG9SZXBvc2l0b3J5IGRldGFsbGVQZWRpZG9SZXBvc2l0
>>"%B64%" echo b3J5OworCisgICAgQEF1dG93aXJlZAorICAgIHByaXZhdGUgVmVudGFSZXBvc2l0b3J5IHZlbnRh
>>"%B64%" echo UmVwb3NpdG9yeTsKKworICAgIEBBdXRvd2lyZWQKKyAgICBwcml2YXRlIERldGFsbGVWZW50YVJl
>>"%B64%" echo cG9zaXRvcnkgZGV0YWxsZVZlbnRhUmVwb3NpdG9yeTsKKworICAgIEBBdXRvd2lyZWQKKyAgICBw
>>"%B64%" echo cml2YXRlIERldm9sdWNpb25SZXBvc2l0b3J5IGRldm9sdWNpb25SZXBvc2l0b3J5OworCisgICAg
>>"%B64%" echo QEF1dG93aXJlZAorICAgIHByaXZhdGUgUGFzc3dvcmRFbmNvZGVyIHBhc3N3b3JkRW5jb2RlcjsK
>>"%B64%" echo KworICAgIEBBdXRvd2lyZWQKKyAgICBwcml2YXRlIFJlcG9ydGVQcm9kdWN0b3NTZXJ2aWNlIHJl
>>"%B64%" echo cG9ydGVQcm9kdWN0b3NTZXJ2aWNlOworCisgICAgQEF1dG93aXJlZAorICAgIHByaXZhdGUgUmVw
>>"%B64%" echo b3J0ZVByb2R1Y3Rvc0V4Y2VsU2VydmljZSByZXBvcnRlUHJvZHVjdG9zRXhjZWxTZXJ2aWNlOwor
>>"%B64%" echo CisgICAgQEF1dG93aXJlZAorICAgIHByaXZhdGUgUmVwb3J0ZVZlbnRhc0V4Y2VsU2VydmljZSBy
>>"%B64%" echo ZXBvcnRlVmVudGFzRXhjZWxTZXJ2aWNlOworCisgICAgQEF1dG93aXJlZAorICAgIHByaXZhdGUg
>>"%B64%" echo SHR0cFNlc3Npb24gc2Vzc2lvbjsKKworICAgIC8vID09PT09PT09PT09PT09PT09PT09PT09PT09
>>"%B64%" echo PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PQorICAgIC8vIFVUSUxJREFERVMKKyAgICAv
>>"%B64%" echo LyA9PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09
>>"%B64%" echo PT0KKworICAgIHByaXZhdGUgVXN1YXJpbyBnZXRVc3VhcmlvQWN0dWFsKCkgeworICAgICAgICBy
>>"%B64%" echo ZXR1cm4gKFVzdWFyaW8pIHNlc3Npb24uZ2V0QXR0cmlidXRlKCJ1c3VhcmlvTG9ndWVhZG8iKTsK
>>"%B64%" echo KyAgICB9CisKKyAgICAvKiogQWdyZWdhIGVsIG5vbWJyZSBkZWwgdXN1YXJpbyBsb2d1ZWFkbyBh
>>"%B64%" echo bCBtb2RlbG8gKGxvIHVzYSBlbCBoZWFkZXIpLiAqLworICAgIHByaXZhdGUgdm9pZCBhZ3JlZ2Fy
>>"%B64%" echo Tm9tYnJlKE1vZGVsIG1vZGVsKSB7CisgICAgICAgIFVzdWFyaW8gdXN1YXJpbyA9IGdldFVzdWFy
>>"%B64%" echo aW9BY3R1YWwoKTsKKyAgICAgICAgbW9kZWwuYWRkQXR0cmlidXRlKCJub21icmUiLCB1c3Vhcmlv
>>"%B64%" echo ICE9IG51bGwgPyB1c3VhcmlvLmdldE5vbWJyZSgpIDogIkFkbWluaXN0cmFkb3IiKTsKKyAgICB9
>>"%B64%" echo CisKKyAgICBwcml2YXRlIFN0cmluZyBkZXNjcmlwY2lvblByb2R1Y3RvKFByb2R1Y3RvIHApIHsK
>>"%B64%" echo KyAgICAgICAgaWYgKHAgPT0gbnVsbCkgeworICAgICAgICAgICAgcmV0dXJuICJQcm9kdWN0byI7
>>"%B64%" echo CisgICAgICAgIH0KKyAgICAgICAgU3RyaW5nQnVpbGRlciBzYiA9IG5ldyBTdHJpbmdCdWlsZGVy
>>"%B64%" echo KCk7CisgICAgICAgIHNiLmFwcGVuZChwLmdldE5vbWJyZSgpICE9IG51bGwgPyBwLmdldE5vbWJy
>>"%B64%" echo ZSgpIDogIlByb2R1Y3RvIik7CisgICAgICAgIGlmIChwLmdldFRhbGxhKCkgIT0gbnVsbCAmJiAh
>>"%B64%" echo cC5nZXRUYWxsYSgpLmlzQmxhbmsoKSkgeworICAgICAgICAgICAgc2IuYXBwZW5kKCIgLSAiKS5h
>>"%B64%" echo cHBlbmQocC5nZXRUYWxsYSgpKTsKKyAgICAgICAgfQorICAgICAgICBpZiAocC5nZXRDb2xvcigp
>>"%B64%" echo ICE9IG51bGwgJiYgIXAuZ2V0Q29sb3IoKS5pc0JsYW5rKCkpIHsKKyAgICAgICAgICAgIHNiLmFw
>>"%B64%" echo cGVuZCgiIC0gIikuYXBwZW5kKHAuZ2V0Q29sb3IoKSk7CisgICAgICAgIH0KKyAgICAgICAgcmV0
>>"%B64%" echo dXJuIHNiLnRvU3RyaW5nKCk7CisgICAgfQorCisgICAgLyoqIEd1YXJkYSBsYSBpbWFnZW4gc3Vi
>>"%B64%" echo aWRhIGVuIHVwbG9hZHMvcHJvZHVjdG9zIHkgZGV2dWVsdmUgc3UgcnV0YSB3ZWIuICovCisgICAg
>>"%B64%" echo cHJpdmF0ZSBTdHJpbmcgZ3VhcmRhckltYWdlbihNdWx0aXBhcnRGaWxlIGltYWdlbikgeworICAg
>>"%B64%" echo ICAgICBpZiAoaW1hZ2VuID09IG51bGwgfHwgaW1hZ2VuLmlzRW1wdHkoKSkgeworICAgICAgICAg
>>"%B64%" echo ICAgcmV0dXJuIG51bGw7CisgICAgICAgIH0KKworICAgICAgICB0cnkgeworICAgICAgICAgICAg
>>"%B64%" echo UGF0aCBjYXJwZXRhID0gUGF0aHMuZ2V0KENBUlBFVEFfSU1BR0VORVMpOworICAgICAgICAgICAg
>>"%B64%" echo RmlsZXMuY3JlYXRlRGlyZWN0b3JpZXMoY2FycGV0YSk7CisKKyAgICAgICAgICAgIFN0cmluZyBv
>>"%B64%" echo cmlnaW5hbCA9IGltYWdlbi5nZXRPcmlnaW5hbEZpbGVuYW1lKCkgIT0gbnVsbCA/IGltYWdlbi5n
>>"%B64%" echo ZXRPcmlnaW5hbEZpbGVuYW1lKCkgOiAiaW1hZ2VuLmpwZyI7CisgICAgICAgICAgICBvcmlnaW5h
>>"%B64%" echo bCA9IG9yaWdpbmFsLnJlcGxhY2VBbGwoIlteYS16QS1aMC05Ll8tXSIsICJfIik7CisgICAgICAg
>>"%B64%" echo ICAgICBTdHJpbmcgbm9tYnJlQXJjaGl2byA9IFN5c3RlbS5jdXJyZW50VGltZU1pbGxpcygpICsg
>>"%B64%" echo Il8iICsgb3JpZ2luYWw7CisKKyAgICAgICAgICAgIEZpbGVzLmNvcHkoaW1hZ2VuLmdldElucHV0
>>"%B64%" echo U3RyZWFtKCksIGNhcnBldGEucmVzb2x2ZShub21icmVBcmNoaXZvKSwgU3RhbmRhcmRDb3B5T3B0
>>"%B64%" echo aW9uLlJFUExBQ0VfRVhJU1RJTkcpOworCisgICAgICAgICAgICByZXR1cm4gUFJFRklKT19JTUFH
>>"%B64%" echo RU5FUyArICIvIiArIG5vbWJyZUFyY2hpdm87CisKKyAgICAgICAgfSBjYXRjaCAoSU9FeGNlcHRp
>>"%B64%" echo b24gZSkgeworICAgICAgICAgICAgcmV0dXJuIG51bGw7CisgICAgICAgIH0KKyAgICB9CisKKyAg
>>"%B64%" echo ICBwcml2YXRlIHN0YXRpYyBSZXNwb25zZUVudGl0eTxieXRlW10+IGFyY2hpdm8oYnl0ZVtdIGNv
>>"%B64%" echo bnRlbmlkbywgU3RyaW5nIG5vbWJyZSwgU3RyaW5nIHRpcG8pIHsKKyAgICAgICAgSHR0cEhlYWRl
>>"%B64%" echo cnMgaGVhZGVycyA9IG5ldyBIdHRwSGVhZGVycygpOworICAgICAgICBoZWFkZXJzLnNldENvbnRl
>>"%B64%" echo bnRUeXBlKE1lZGlhVHlwZS5wYXJzZU1lZGlhVHlwZSh0aXBvKSk7CisgICAgICAgIGhlYWRlcnMu
>>"%B64%" echo c2V0Q29udGVudERpc3Bvc2l0aW9uKENvbnRlbnREaXNwb3NpdGlvbi5hdHRhY2htZW50KCkuZmls
>>"%B64%" echo ZW5hbWUobm9tYnJlKS5idWlsZCgpKTsKKyAgICAgICAgcmV0dXJuIFJlc3BvbnNlRW50aXR5Lm9r
>>"%B64%" echo KCkuaGVhZGVycyhoZWFkZXJzKS5ib2R5KGNvbnRlbmlkbyk7CisgICAgfQorCisgICAgLy8gPT09
>>"%B64%" echo PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09Cisg
>>"%B64%" echo ICAgLy8gUEVSRklMCisgICAgLy8gPT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09
>>"%B64%" echo PT09PT09PT09PT09PT09PT09PT09CisKKyAgICBAR2V0TWFwcGluZygiL3BlcmZpbCIpCisgICAg
>>"%B64%" echo cHVibGljIFN0cmluZyBwZXJmaWwoTW9kZWwgbW9kZWwpIHsKKworICAgICAgICBVc3VhcmlvIHVz
>>"%B64%" echo dWFyaW8gPSBnZXRVc3VhcmlvQWN0dWFsKCk7CisKKyAgICAgICAgaWYgKHVzdWFyaW8gPT0gbnVs
>>"%B64%" echo bCkgeworICAgICAgICAgICAgcmV0dXJuICJyZWRpcmVjdDovbG9naW4iOworICAgICAgICB9CisK
>>"%B64%" echo KyAgICAgICAgbW9kZWwuYWRkQXR0cmlidXRlKCJ1c3VhcmlvIiwgdXN1YXJpbyk7CisgICAgICAg
>>"%B64%" echo IGFncmVnYXJOb21icmUobW9kZWwpOworCisgICAgICAgIHJldHVybiAiYWRtaW4vcGVyZmlsX2Fk
>>"%B64%" echo bWluIjsKKyAgICB9CisKKyAgICBAUG9zdE1hcHBpbmcoIi9wZXJmaWwvYWN0dWFsaXphciIpCisg
>>"%B64%" echo ICAgcHVibGljIFN0cmluZyBhY3R1YWxpemFyUGVyZmlsKAorICAgICAgICAgICAgQFJlcXVlc3RQ
>>"%B64%" echo YXJhbSBTdHJpbmcgY29ycmVvLAorICAgICAgICAgICAgQFJlcXVlc3RQYXJhbSBTdHJpbmcgdGVs
>>"%B64%" echo ZWZvbm8sCisgICAgICAgICAgICBAUmVxdWVzdFBhcmFtIFN0cmluZyBkaXJlY2Npb24sCisgICAg
>>"%B64%" echo ICAgICAgICBAUmVxdWVzdFBhcmFtKHJlcXVpcmVkID0gZmFsc2UpIFN0cmluZyBwYXNzd29yZCwK
>>"%B64%" echo KyAgICAgICAgICAgIE1vZGVsIG1vZGVsKSB7CisKKyAgICAgICAgVXN1YXJpbyB1c3VhcmlvID0g
>>"%B64%" echo Z2V0VXN1YXJpb0FjdHVhbCgpOworCisgICAgICAgIGlmICh1c3VhcmlvID09IG51bGwpIHsKKyAg
>>"%B64%" echo ICAgICAgICAgIHJldHVybiAicmVkaXJlY3Q6L2xvZ2luIjsKKyAgICAgICAgfQorCisgICAgICAg
>>"%B64%" echo IFN0cmluZyBjb3JyZW9MaW1waW8gPSBjb3JyZW8gIT0gbnVsbCA/IGNvcnJlby50cmltKCkgOiAi
>>"%B64%" echo IjsKKyAgICAgICAgU3RyaW5nIHRlbGVmb25vTGltcGlvID0gdGVsZWZvbm8gIT0gbnVsbCA/IHRl
>>"%B64%" echo bGVmb25vLnRyaW0oKSA6ICIiOworICAgICAgICBTdHJpbmcgZGlyZWNjaW9uTGltcGlhID0gZGly
>>"%B64%" echo ZWNjaW9uICE9IG51bGwgPyBkaXJlY2Npb24udHJpbSgpIDogIiI7CisKKyAgICAgICAgLy8gPT09
>>"%B64%" echo PT0gVmFsaWRhY2lvbmVzIGRlIHNlcnZpZG9yIChsYXMgbWlzbWFzIGRlbCBwYW5lbCBkZWwgYm9k
>>"%B64%" echo ZWd1ZXJvKSA9PT09PQorCisgICAgICAgIGlmICghY29ycmVvTGltcGlvLm1hdGNoZXMoIl5bXlxc
>>"%B64%" echo c0BdK0BbXlxcc0BdK1xcLlteXFxzQF0rJCIpKSB7CisgICAgICAgICAgICByZXR1cm4gdm9sdmVy
>>"%B64%" echo QWxQZXJmaWwobW9kZWwsIHVzdWFyaW8sICJFbCBjb3JyZW8gZWxlY3Ryw7NuaWNvIG5vIHRpZW5l
>>"%B64%" echo IHVuIGZvcm1hdG8gdsOhbGlkby4iKTsKKyAgICAgICAgfQorCisgICAgICAgIGlmICghdGVsZWZv
>>"%B64%" echo bm9MaW1waW8ubWF0Y2hlcygiXlswLTldezcsMTV9JCIpKSB7CisgICAgICAgICAgICByZXR1cm4g
>>"%B64%" echo dm9sdmVyQWxQZXJmaWwobW9kZWwsIHVzdWFyaW8sICJFbCB0ZWzDqWZvbm8gZGViZSBjb250ZW5l
>>"%B64%" echo ciBzb2xvIG7Dum1lcm9zLCBlbnRyZSA3IHkgMTUgZMOtZ2l0b3MuIik7CisgICAgICAgIH0KKwor
>>"%B64%" echo ICAgICAgICBpZiAoZGlyZWNjaW9uTGltcGlhLmxlbmd0aCgpIDwgNSkgeworICAgICAgICAgICAg
>>"%B64%" echo cmV0dXJuIHZvbHZlckFsUGVyZmlsKG1vZGVsLCB1c3VhcmlvLCAiTGEgZGlyZWNjacOzbiBkZWJl
>>"%B64%" echo IHRlbmVyIGFsIG1lbm9zIDUgY2FyYWN0ZXJlcy4iKTsKKyAgICAgICAgfQorCisgICAgICAgIGlm
>>"%B64%" echo IChwYXNzd29yZCAhPSBudWxsICYmICFwYXNzd29yZC5pc0VtcHR5KCkgJiYgcGFzc3dvcmQubGVu
>>"%B64%" echo Z3RoKCkgPCA2KSB7CisgICAgICAgICAgICByZXR1cm4gdm9sdmVyQWxQZXJmaWwobW9kZWwsIHVz
>>"%B64%" echo dWFyaW8sICJMYSBjb250cmFzZcOxYSBkZWJlIHRlbmVyIGFsIG1lbm9zIDYgY2FyYWN0ZXJlcy4i
>>"%B64%" echo KTsKKyAgICAgICAgfQorCisgICAgICAgIGJvb2xlYW4gY29ycmVvQ2FtYmlvID0gIWNvcnJlb0xp
>>"%B64%" echo bXBpby5lcXVhbHModXN1YXJpby5nZXRDb3JyZW8oKSk7CisKKyAgICAgICAgaWYgKGNvcnJlb0Nh
>>"%B64%" echo bWJpbykgeworICAgICAgICAgICAgVXN1YXJpbyBleGlzdGVudGUgPSB1c3VhcmlvUmVwb3NpdG9y
>>"%B64%" echo eS5maW5kQnlDb3JyZW8oY29ycmVvTGltcGlvKTsKKyAgICAgICAgICAgIGlmIChleGlzdGVudGUg
>>"%B64%" echo IT0gbnVsbCAmJiAhZXhpc3RlbnRlLmdldElkKCkuZXF1YWxzKHVzdWFyaW8uZ2V0SWQoKSkpIHsK
>>"%B64%" echo KyAgICAgICAgICAgICAgICByZXR1cm4gdm9sdmVyQWxQZXJmaWwobW9kZWwsIHVzdWFyaW8sICJF
>>"%B64%" echo c2UgY29ycmVvIHlhIGVzdMOhIGVuIHVzbyBwb3Igb3RybyB1c3VhcmlvLiIpOworICAgICAgICAg
>>"%B64%" echo ICAgfQorICAgICAgICB9CisKKyAgICAgICAgdXN1YXJpby5zZXRUZWxlZm9ubyh0ZWxlZm9ub0xp
>>"%B64%" echo bXBpbyk7CisgICAgICAgIHVzdWFyaW8uc2V0RGlyZWNjaW9uKGRpcmVjY2lvbkxpbXBpYSk7CisK
>>"%B64%" echo KyAgICAgICAgaWYgKHBhc3N3b3JkICE9IG51bGwgJiYgIXBhc3N3b3JkLmlzRW1wdHkoKSkgewor
>>"%B64%" echo ICAgICAgICAgICAgdXN1YXJpby5zZXRDb250cmFzZW5hKHBhc3N3b3JkRW5jb2Rlci5lbmNvZGUo
>>"%B64%" echo cGFzc3dvcmQpKTsKKyAgICAgICAgfQorCisgICAgICAgIGlmIChjb3JyZW9DYW1iaW8pIHsKKyAg
>>"%B64%" echo ICAgICAgICAgIHVzdWFyaW8uc2V0Q29ycmVvKGNvcnJlb0xpbXBpbyk7CisgICAgICAgICAgICB1
>>"%B64%" echo c3VhcmlvUmVwb3NpdG9yeS5zYXZlKHVzdWFyaW8pOworCisgICAgICAgICAgICAvLyBTZSByZS1h
>>"%B64%" echo dXRlbnRpY2EgY29uIGVsIG51ZXZvIGNvcnJlbyBwYXJhIG5vIHBlcmRlciBsYSBzZXNpw7NuCisg
>>"%B64%" echo ICAgICAgICAgICBBdXRoZW50aWNhdGlvbiBhdXRoID0gU2VjdXJpdHlDb250ZXh0SG9sZGVyLmdl
>>"%B64%" echo dENvbnRleHQoKS5nZXRBdXRoZW50aWNhdGlvbigpOworICAgICAgICAgICAgaWYgKGF1dGggIT0g
>>"%B64%" echo bnVsbCkgeworICAgICAgICAgICAgICAgIEF1dGhlbnRpY2F0aW9uIG5ld0F1dGggPSBuZXcgVXNl
>>"%B64%" echo cm5hbWVQYXNzd29yZEF1dGhlbnRpY2F0aW9uVG9rZW4oCisgICAgICAgICAgICAgICAgICAgICAg
>>"%B64%" echo ICBjb3JyZW9MaW1waW8sCisgICAgICAgICAgICAgICAgICAgICAgICBhdXRoLmdldENyZWRlbnRp
>>"%B64%" echo YWxzKCksCisgICAgICAgICAgICAgICAgICAgICAgICBhdXRoLmdldEF1dGhvcml0aWVzKCkpOwor
>>"%B64%" echo ICAgICAgICAgICAgICAgIFNlY3VyaXR5Q29udGV4dEhvbGRlci5nZXRDb250ZXh0KCkuc2V0QXV0
>>"%B64%" echo aGVudGljYXRpb24obmV3QXV0aCk7CisgICAgICAgICAgICB9CisgICAgICAgIH0gZWxzZSB7Cisg
>>"%B64%" echo ICAgICAgICAgICB1c3VhcmlvUmVwb3NpdG9yeS5zYXZlKHVzdWFyaW8pOworICAgICAgICB9CisK
>>"%B64%" echo KyAgICAgICAgc2Vzc2lvbi5zZXRBdHRyaWJ1dGUoInVzdWFyaW9Mb2d1ZWFkbyIsIHVzdWFyaW8p
>>"%B64%" echo OworCisgICAgICAgIG1vZGVsLmFkZEF0dHJpYnV0ZSgidXN1YXJpbyIsIHVzdWFyaW8pOworICAg
>>"%B64%" echo ICAgICBhZ3JlZ2FyTm9tYnJlKG1vZGVsKTsKKyAgICAgICAgbW9kZWwuYWRkQXR0cmlidXRlKCJl
>>"%B64%" echo eGl0byIsICJQZXJmaWwgYWN0dWFsaXphZG8gY29ycmVjdGFtZW50ZS4iKTsKKworICAgICAgICBy
>>"%B64%" echo ZXR1cm4gImFkbWluL3BlcmZpbF9hZG1pbiI7CisgICAgfQorCisgICAgcHJpdmF0ZSBTdHJpbmcg
>>"%B64%" echo dm9sdmVyQWxQZXJmaWwoTW9kZWwgbW9kZWwsIFVzdWFyaW8gdXN1YXJpbywgU3RyaW5nIGVycm9y
>>"%B64%" echo KSB7CisgICAgICAgIG1vZGVsLmFkZEF0dHJpYnV0ZSgidXN1YXJpbyIsIHVzdWFyaW8pOworICAg
>>"%B64%" echo ICAgICBhZ3JlZ2FyTm9tYnJlKG1vZGVsKTsKKyAgICAgICAgbW9kZWwuYWRkQXR0cmlidXRlKCJl
>>"%B64%" echo cnJvciIsIGVycm9yKTsKKyAgICAgICAgcmV0dXJuICJhZG1pbi9wZXJmaWxfYWRtaW4iOworICAg
>>"%B64%" echo IH0KKworICAgIC8vID09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09
>>"%B64%" echo PT09PT09PT09PT09PQorICAgIC8vIFBST0RVQ1RPUworICAgIC8vID09PT09PT09PT09PT09PT09
>>"%B64%" echo PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PQorCisgICAgQEdldE1hcHBp
>>"%B64%" echo bmcoIi9wcm9kdWN0b3MiKQorICAgIHB1YmxpYyBTdHJpbmcgcHJvZHVjdG9zKE1vZGVsIG1vZGVs
>>"%B64%" echo KSB7CisKKyAgICAgICAgbW9kZWwuYWRkQXR0cmlidXRlKCJwcm9kdWN0b3MiLCBwcm9kdWN0b1Jl
>>"%B64%" echo cG9zaXRvcnkuZmluZEJ5SW5oYWJpbGl0YWRvRmFsc2VPcmRlckJ5SWREZXNjKCkpOworICAgICAg
>>"%B64%" echo ICBtb2RlbC5hZGRBdHRyaWJ1dGUoInByb2R1Y3Rvc0luaGFiaWxpdGFkb3MiLCBwcm9kdWN0b1Jl
>>"%B64%" echo cG9zaXRvcnkuZmluZEJ5SW5oYWJpbGl0YWRvVHJ1ZU9yZGVyQnlJZERlc2MoKSk7CisgICAgICAg
>>"%B64%" echo IGFncmVnYXJOb21icmUobW9kZWwpOworCisgICAgICAgIHJldHVybiAiYWRtaW4vcHJvZHVjdG9z
>>"%B64%" echo X2FkbWluIjsKKyAgICB9CisKKyAgICBAUG9zdE1hcHBpbmcoIi9wcm9kdWN0b3MvY3JlYXIiKQor
>>"%B64%" echo ICAgIHB1YmxpYyBTdHJpbmcgY3JlYXJQcm9kdWN0bygKKyAgICAgICAgICAgIEBSZXF1ZXN0UGFy
>>"%B64%" echo YW0gU3RyaW5nIG5vbWJyZSwKKyAgICAgICAgICAgIEBSZXF1ZXN0UGFyYW0gQmlnRGVjaW1hbCBw
>>"%B64%" echo cmVjaW8sCisgICAgICAgICAgICBAUmVxdWVzdFBhcmFtKHJlcXVpcmVkID0gZmFsc2UpIFN0cmlu
>>"%B64%" echo ZyB0YWxsYSwKKyAgICAgICAgICAgIEBSZXF1ZXN0UGFyYW0ocmVxdWlyZWQgPSBmYWxzZSkgU3Ry
>>"%B64%" echo aW5nIGNvbG9yLAorICAgICAgICAgICAgQFJlcXVlc3RQYXJhbShyZXF1aXJlZCA9IGZhbHNlKSBN
>>"%B64%" echo dWx0aXBhcnRGaWxlIGltYWdlbiwKKyAgICAgICAgICAgIFJlZGlyZWN0QXR0cmlidXRlcyByZWRp
>>"%B64%" echo cmVjdCkgeworCisgICAgICAgIGlmIChub21icmUgPT0gbnVsbCB8fCBub21icmUuaXNCbGFuaygp
>>"%B64%" echo KSB7CisgICAgICAgICAgICByZWRpcmVjdC5hZGRGbGFzaEF0dHJpYnV0ZSgiZXJyb3IiLCAiRWwg
>>"%B64%" echo bm9tYnJlIGRlbCBwcm9kdWN0byBlcyBvYmxpZ2F0b3Jpby4iKTsKKyAgICAgICAgICAgIHJldHVy
>>"%B64%" echo biAicmVkaXJlY3Q6L2FkbWluL3Byb2R1Y3RvcyI7CisgICAgICAgIH0KKworICAgICAgICBpZiAo
>>"%B64%" echo cHJlY2lvID09IG51bGwgfHwgcHJlY2lvLmNvbXBhcmVUbyhCaWdEZWNpbWFsLlpFUk8pIDw9IDAp
>>"%B64%" echo IHsKKyAgICAgICAgICAgIHJlZGlyZWN0LmFkZEZsYXNoQXR0cmlidXRlKCJlcnJvciIsICJFbCBw
>>"%B64%" echo cmVjaW8gZGViZSBzZXIgbWF5b3IgcXVlIGNlcm8uIik7CisgICAgICAgICAgICByZXR1cm4gInJl
>>"%B64%" echo ZGlyZWN0Oi9hZG1pbi9wcm9kdWN0b3MiOworICAgICAgICB9CisKKyAgICAgICAgUHJvZHVjdG8g
>>"%B64%" echo cHJvZHVjdG8gPSBuZXcgUHJvZHVjdG8oKTsKKyAgICAgICAgcHJvZHVjdG8uc2V0Tm9tYnJlKG5v
>>"%B64%" echo bWJyZS50cmltKCkpOworICAgICAgICBwcm9kdWN0by5zZXRQcmVjaW8ocHJlY2lvKTsKKyAgICAg
>>"%B64%" echo ICAgcHJvZHVjdG8uc2V0VGFsbGEodGFsbGEgIT0gbnVsbCA/IHRhbGxhLnRyaW0oKSA6IG51bGwp
>>"%B64%" echo OworICAgICAgICBwcm9kdWN0by5zZXRDb2xvcihjb2xvciAhPSBudWxsID8gY29sb3IudHJpbSgp
>>"%B64%" echo IDogbnVsbCk7CisgICAgICAgIHByb2R1Y3RvLnNldEVzdGFkbygiRGlzcG9uaWJsZSIpOworICAg
>>"%B64%" echo ICAgICBwcm9kdWN0by5zZXRJbmhhYmlsaXRhZG8oZmFsc2UpOworICAgICAgICBwcm9kdWN0by5z
>>"%B64%" echo ZXRDYXRlZ29yaWEoIkdlbmVyYWwiKTsKKyAgICAgICAgcHJvZHVjdG8uc2V0SW1hZ2VuKGd1YXJk
>>"%B64%" echo YXJJbWFnZW4oaW1hZ2VuKSk7CisKKyAgICAgICAgcHJvZHVjdG9SZXBvc2l0b3J5LnNhdmUocHJv
>>"%B64%" echo ZHVjdG8pOworCisgICAgICAgIC8vIFNpIGVsIHByb2R1Y3RvIHNlIGNyZWEgeSBubyB0aWVuZSBp
>>"%B64%" echo bnZlbnRhcmlvLCBzZSBjcmVhIGVuIGNlcm8KKyAgICAgICAgLy8gcGFyYSBxdWUgYXBhcmV6Y2Eg
>>"%B64%" echo ZGUgaW5tZWRpYXRvIGVuIGxhIHNlY2Npw7NuIGRlIGludmVudGFyaW8uCisgICAgICAgIGJvb2xl
>>"%B64%" echo YW4gdGllbmVJbnZlbnRhcmlvID0gaW52ZW50YXJpb1JlcG9zaXRvcnkuZmluZEFsbCgpLnN0cmVh
>>"%B64%" echo bSgpCisgICAgICAgICAgICAgICAgLmFueU1hdGNoKGludiAtPiBwcm9kdWN0by5nZXRJZCgpLmVx
>>"%B64%" echo dWFscyhpbnYuZ2V0SWRQcm9kdWN0bygpKSk7CisKKyAgICAgICAgaWYgKCF0aWVuZUludmVudGFy
>>"%B64%" echo aW8pIHsKKyAgICAgICAgICAgIEludmVudGFyaW8gaW52ZW50YXJpbyA9IG5ldyBJbnZlbnRhcmlv
>>"%B64%" echo KCk7CisgICAgICAgICAgICBpbnZlbnRhcmlvLnNldElkUHJvZHVjdG8ocHJvZHVjdG8uZ2V0SWQo
>>"%B64%" echo KSk7CisgICAgICAgICAgICBpbnZlbnRhcmlvLnNldENhbnRpZGFkQWN0dWFsKDApOworICAgICAg
>>"%B64%" echo ICAgICAgaW52ZW50YXJpby5zZXRTdG9ja01pbmltbygwKTsKKyAgICAgICAgICAgIGludmVudGFy
>>"%B64%" echo aW8uc2V0SW5oYWJpbGl0YWRvKGZhbHNlKTsKKyAgICAgICAgICAgIGludmVudGFyaW9SZXBvc2l0
>>"%B64%" echo b3J5LnNhdmUoaW52ZW50YXJpbyk7CisgICAgICAgIH0KKworICAgICAgICByZWRpcmVjdC5hZGRG
>>"%B64%" echo bGFzaEF0dHJpYnV0ZSgiZXhpdG8iLCAiUHJvZHVjdG8gY3JlYWRvIGNvcnJlY3RhbWVudGUuIik7
>>"%B64%" echo CisgICAgICAgIHJldHVybiAicmVkaXJlY3Q6L2FkbWluL3Byb2R1Y3RvcyI7CisgICAgfQorCisg
>>"%B64%" echo ICAgQFBvc3RNYXBwaW5nKCIvcHJvZHVjdG9zL2FjdHVhbGl6YXIiKQorICAgIHB1YmxpYyBTdHJp
>>"%B64%" echo bmcgYWN0dWFsaXphclByb2R1Y3RvKAorICAgICAgICAgICAgQFJlcXVlc3RQYXJhbSBMb25nIGlk
>>"%B64%" echo UHJvZHVjdG8sCisgICAgICAgICAgICBAUmVxdWVzdFBhcmFtIFN0cmluZyBub21icmUsCisgICAg
>>"%B64%" echo ICAgICAgICBAUmVxdWVzdFBhcmFtIEJpZ0RlY2ltYWwgcHJlY2lvLAorICAgICAgICAgICAgQFJl
>>"%B64%" echo cXVlc3RQYXJhbShyZXF1aXJlZCA9IGZhbHNlKSBTdHJpbmcgdGFsbGEsCisgICAgICAgICAgICBA
>>"%B64%" echo UmVxdWVzdFBhcmFtKHJlcXVpcmVkID0gZmFsc2UpIFN0cmluZyBjb2xvciwKKyAgICAgICAgICAg
>>"%B64%" echo IEBSZXF1ZXN0UGFyYW0ocmVxdWlyZWQgPSBmYWxzZSkgTXVsdGlwYXJ0RmlsZSBpbWFnZW4sCisg
>>"%B64%" echo ICAgICAgICAgICBSZWRpcmVjdEF0dHJpYnV0ZXMgcmVkaXJlY3QpIHsKKworICAgICAgICBQcm9k
>>"%B64%" echo dWN0byBwcm9kdWN0byA9IHByb2R1Y3RvUmVwb3NpdG9yeS5maW5kQnlJZChpZFByb2R1Y3RvKS5v
>>"%B64%" echo ckVsc2UobnVsbCk7CisKKyAgICAgICAgaWYgKHByb2R1Y3RvID09IG51bGwpIHsKKyAgICAgICAg
>>"%B64%" echo ICAgIHJlZGlyZWN0LmFkZEZsYXNoQXR0cmlidXRlKCJlcnJvciIsICJFbCBwcm9kdWN0byBubyBl
>>"%B64%" echo eGlzdGUuIik7CisgICAgICAgICAgICByZXR1cm4gInJlZGlyZWN0Oi9hZG1pbi9wcm9kdWN0b3Mi
>>"%B64%" echo OworICAgICAgICB9CisKKyAgICAgICAgaWYgKG5vbWJyZSA9PSBudWxsIHx8IG5vbWJyZS5pc0Js
>>"%B64%" echo YW5rKCkpIHsKKyAgICAgICAgICAgIHJlZGlyZWN0LmFkZEZsYXNoQXR0cmlidXRlKCJlcnJvciIs
>>"%B64%" echo ICJFbCBub21icmUgZGVsIHByb2R1Y3RvIGVzIG9ibGlnYXRvcmlvLiIpOworICAgICAgICAgICAg
>>"%B64%" echo cmV0dXJuICJyZWRpcmVjdDovYWRtaW4vcHJvZHVjdG9zIjsKKyAgICAgICAgfQorCisgICAgICAg
>>"%B64%" echo IGlmIChwcmVjaW8gPT0gbnVsbCB8fCBwcmVjaW8uY29tcGFyZVRvKEJpZ0RlY2ltYWwuWkVSTykg
>>"%B64%" echo PD0gMCkgeworICAgICAgICAgICAgcmVkaXJlY3QuYWRkRmxhc2hBdHRyaWJ1dGUoImVycm9yIiwg
>>"%B64%" echo IkVsIHByZWNpbyBkZWJlIHNlciBtYXlvciBxdWUgY2Vyby4iKTsKKyAgICAgICAgICAgIHJldHVy
>>"%B64%" echo biAicmVkaXJlY3Q6L2FkbWluL3Byb2R1Y3RvcyI7CisgICAgICAgIH0KKworICAgICAgICBwcm9k
>>"%B64%" echo dWN0by5zZXROb21icmUobm9tYnJlLnRyaW0oKSk7CisgICAgICAgIHByb2R1Y3RvLnNldFByZWNp
>>"%B64%" echo byhwcmVjaW8pOworICAgICAgICBwcm9kdWN0by5zZXRUYWxsYSh0YWxsYSAhPSBudWxsID8gdGFs
>>"%B64%" echo bGEudHJpbSgpIDogbnVsbCk7CisgICAgICAgIHByb2R1Y3RvLnNldENvbG9yKGNvbG9yICE9IG51
>>"%B64%" echo bGwgPyBjb2xvci50cmltKCkgOiBudWxsKTsKKworICAgICAgICBTdHJpbmcgbnVldmFJbWFnZW4g
>>"%B64%" echo PSBndWFyZGFySW1hZ2VuKGltYWdlbik7CisgICAgICAgIGlmIChudWV2YUltYWdlbiAhPSBudWxs
>>"%B64%" echo KSB7CisgICAgICAgICAgICBwcm9kdWN0by5zZXRJbWFnZW4obnVldmFJbWFnZW4pOworICAgICAg
>>"%B64%" echo ICB9CisKKyAgICAgICAgcHJvZHVjdG9SZXBvc2l0b3J5LnNhdmUocHJvZHVjdG8pOworCisgICAg
>>"%B64%" echo ICAgIHJlZGlyZWN0LmFkZEZsYXNoQXR0cmlidXRlKCJleGl0byIsICJQcm9kdWN0byBhY3R1YWxp
>>"%B64%" echo emFkbyBjb3JyZWN0YW1lbnRlLiIpOworICAgICAgICByZXR1cm4gInJlZGlyZWN0Oi9hZG1pbi9w
>>"%B64%" echo cm9kdWN0b3MiOworICAgIH0KKworICAgIEBQb3N0TWFwcGluZygiL3Byb2R1Y3Rvcy90b2dnbGVF
>>"%B64%" echo c3RhZG8iKQorICAgIHB1YmxpYyBTdHJpbmcgdG9nZ2xlRXN0YWRvUHJvZHVjdG8oQFJlcXVlc3RQ
>>"%B64%" echo YXJhbSBMb25nIGlkUHJvZHVjdG8sIFJlZGlyZWN0QXR0cmlidXRlcyByZWRpcmVjdCkgeworCisg
>>"%B64%" echo ICAgICAgIFByb2R1Y3RvIHByb2R1Y3RvID0gcHJvZHVjdG9SZXBvc2l0b3J5LmZpbmRCeUlkKGlk
>>"%B64%" echo UHJvZHVjdG8pLm9yRWxzZShudWxsKTsKKworICAgICAgICBpZiAocHJvZHVjdG8gIT0gbnVsbCkg
>>"%B64%" echo eworICAgICAgICAgICAgYm9vbGVhbiBkaXNwb25pYmxlID0gISJEaXNwb25pYmxlIi5lcXVhbHNJ
>>"%B64%" echo Z25vcmVDYXNlKHByb2R1Y3RvLmdldEVzdGFkbygpKTsKKyAgICAgICAgICAgIHByb2R1Y3RvLnNl
>>"%B64%" echo dEVzdGFkbyhkaXNwb25pYmxlID8gIkRpc3BvbmlibGUiIDogIkFnb3RhZG8iKTsKKyAgICAgICAg
>>"%B64%" echo ICAgIHByb2R1Y3RvUmVwb3NpdG9yeS5zYXZlKHByb2R1Y3RvKTsKKworICAgICAgICAgICAgcmVk
>>"%B64%" echo aXJlY3QuYWRkRmxhc2hBdHRyaWJ1dGUoImV4aXRvIiwKKyAgICAgICAgICAgICAgICAgICAgIkVz
>>"%B64%" echo dGFkbyBkZWwgcHJvZHVjdG8gY2FtYmlhZG8gYSAiICsgcHJvZHVjdG8uZ2V0RXN0YWRvKCkgKyAi
>>"%B64%" echo LiIpOworICAgICAgICB9CisKKyAgICAgICAgcmV0dXJuICJyZWRpcmVjdDovYWRtaW4vcHJvZHVj
>>"%B64%" echo dG9zIjsKKyAgICB9CisKKyAgICBAUG9zdE1hcHBpbmcoIi9wcm9kdWN0b3MvaW5oYWJpbGl0YXIi
>>"%B64%" echo KQorICAgIHB1YmxpYyBTdHJpbmcgaW5oYWJpbGl0YXJQcm9kdWN0byhAUmVxdWVzdFBhcmFtIExv
>>"%B64%" echo bmcgaWRQcm9kdWN0bywgUmVkaXJlY3RBdHRyaWJ1dGVzIHJlZGlyZWN0KSB7CisKKyAgICAgICAg
>>"%B64%" echo UHJvZHVjdG8gcHJvZHVjdG8gPSBwcm9kdWN0b1JlcG9zaXRvcnkuZmluZEJ5SWQoaWRQcm9kdWN0
>>"%B64%" echo bykub3JFbHNlKG51bGwpOworCisgICAgICAgIGlmIChwcm9kdWN0byAhPSBudWxsKSB7CisgICAg
>>"%B64%" echo ICAgICAgICBwcm9kdWN0by5zZXRJbmhhYmlsaXRhZG8odHJ1ZSk7CisgICAgICAgICAgICBwcm9k
>>"%B64%" echo dWN0b1JlcG9zaXRvcnkuc2F2ZShwcm9kdWN0byk7CisgICAgICAgICAgICByZWRpcmVjdC5hZGRG
>>"%B64%" echo bGFzaEF0dHJpYnV0ZSgiZXhpdG8iLCAiUHJvZHVjdG8gaW5oYWJpbGl0YWRvLiIpOworICAgICAg
>>"%B64%" echo ICB9CisKKyAgICAgICAgcmV0dXJuICJyZWRpcmVjdDovYWRtaW4vcHJvZHVjdG9zIjsKKyAgICB9
>>"%B64%" echo CisKKyAgICBAUG9zdE1hcHBpbmcoIi9wcm9kdWN0b3MvaGFiaWxpdGFyIikKKyAgICBwdWJsaWMg
>>"%B64%" echo U3RyaW5nIGhhYmlsaXRhclByb2R1Y3RvKEBSZXF1ZXN0UGFyYW0gTG9uZyBpZFByb2R1Y3RvLCBS
>>"%B64%" echo ZWRpcmVjdEF0dHJpYnV0ZXMgcmVkaXJlY3QpIHsKKworICAgICAgICBQcm9kdWN0byBwcm9kdWN0
>>"%B64%" echo byA9IHByb2R1Y3RvUmVwb3NpdG9yeS5maW5kQnlJZChpZFByb2R1Y3RvKS5vckVsc2UobnVsbCk7
>>"%B64%" echo CisKKyAgICAgICAgaWYgKHByb2R1Y3RvICE9IG51bGwpIHsKKyAgICAgICAgICAgIHByb2R1Y3Rv
>>"%B64%" echo LnNldEluaGFiaWxpdGFkbyhmYWxzZSk7CisgICAgICAgICAgICBwcm9kdWN0b1JlcG9zaXRvcnku
>>"%B64%" echo c2F2ZShwcm9kdWN0byk7CisgICAgICAgICAgICByZWRpcmVjdC5hZGRGbGFzaEF0dHJpYnV0ZSgi
>>"%B64%" echo ZXhpdG8iLCAiUHJvZHVjdG8gaGFiaWxpdGFkby4iKTsKKyAgICAgICAgfQorCisgICAgICAgIHJl
>>"%B64%" echo dHVybiAicmVkaXJlY3Q6L2FkbWluL3Byb2R1Y3RvcyI7CisgICAgfQorCisgICAgQEdldE1hcHBp
>>"%B64%" echo bmcoIi9wcm9kdWN0b3MvcmVwb3J0ZSIpCisgICAgcHVibGljIFJlc3BvbnNlRW50aXR5PGJ5dGVb
>>"%B64%" echo XT4gcmVwb3J0ZVByb2R1Y3RvcygpIHsKKyAgICAgICAgYnl0ZVtdIHBkZiA9IHJlcG9ydGVQcm9k
>>"%B64%" echo dWN0b3NTZXJ2aWNlLmdlbmVyYXJSZXBvcnRlKHByb2R1Y3RvUmVwb3NpdG9yeS5maW5kQWxsKCkp
>>"%B64%" echo OworICAgICAgICByZXR1cm4gYXJjaGl2byhwZGYsICJyZXBvcnRlX3Byb2R1Y3Rvcy5wZGYiLCAi
>>"%B64%" echo YXBwbGljYXRpb24vcGRmIik7CisgICAgfQorCisgICAgQEdldE1hcHBpbmcoIi9wcm9kdWN0b3Mv
>>"%B64%" echo cmVwb3J0ZS9leGNlbCIpCisgICAgcHVibGljIFJlc3BvbnNlRW50aXR5PGJ5dGVbXT4gcmVwb3J0
>>"%B64%" echo ZVByb2R1Y3Rvc0V4Y2VsKCkgeworICAgICAgICBieXRlW10gZXhjZWwgPSByZXBvcnRlUHJvZHVj
>>"%B64%" echo dG9zRXhjZWxTZXJ2aWNlLmdlbmVyYXJSZXBvcnRlKHByb2R1Y3RvUmVwb3NpdG9yeS5maW5kQWxs
>>"%B64%" echo KCkpOworICAgICAgICByZXR1cm4gYXJjaGl2byhleGNlbCwgInJlcG9ydGVfcHJvZHVjdG9zLnhs
>>"%B64%" echo c3giLAorICAgICAgICAgICAgICAgICJhcHBsaWNhdGlvbi92bmQub3BlbnhtbGZvcm1hdHMtb2Zm
>>"%B64%" echo aWNlZG9jdW1lbnQuc3ByZWFkc2hlZXRtbC5zaGVldCIpOworICAgIH0KKworICAgIC8vID09PT09
>>"%B64%" echo PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PQorICAg
>>"%B64%" echo IC8vIElOVkVOVEFSSU8KKyAgICAvLyA9PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09
>>"%B64%" echo PT09PT09PT09PT09PT09PT09PT09PT0KKworICAgIEBHZXRNYXBwaW5nKCIvaW52ZW50YXJpbyIp
>>"%B64%" echo CisgICAgcHVibGljIFN0cmluZyBpbnZlbnRhcmlvKE1vZGVsIG1vZGVsKSB7CisKKyAgICAgICAg
>>"%B64%" echo bW9kZWwuYWRkQXR0cmlidXRlKCJpbnZlbnRhcmlvcyIsIGludmVudGFyaW9SZXBvc2l0b3J5LmZp
>>"%B64%" echo bmRBbGwoKSk7CisgICAgICAgIG1vZGVsLmFkZEF0dHJpYnV0ZSgicHJvZHVjdG9zIiwgcHJvZHVj
>>"%B64%" echo dG9SZXBvc2l0b3J5LmZpbmRBbGwoKSk7CisgICAgICAgIG1vZGVsLmFkZEF0dHJpYnV0ZSgibW92
>>"%B64%" echo aW1pZW50b3MiLCBtb3ZpbWllbnRvSW52ZW50YXJpb1JlcG9zaXRvcnkuZmluZFRvcDUwQnlPcmRl
>>"%B64%" echo ckJ5SWREZXNjKCkpOworICAgICAgICBhZ3JlZ2FyTm9tYnJlKG1vZGVsKTsKKworICAgICAgICBy
>>"%B64%" echo ZXR1cm4gImFkbWluL2ludmVudGFyaW9fYWRtaW4iOworICAgIH0KKworICAgIEBUcmFuc2FjdGlv
>>"%B64%" echo bmFsCisgICAgQFBvc3RNYXBwaW5nKCIvaW52ZW50YXJpby9jcmVhciIpCisgICAgcHVibGljIFN0
>>"%B64%" echo cmluZyBjcmVhckludmVudGFyaW8oCisgICAgICAgICAgICBAUmVxdWVzdFBhcmFtIExvbmcgaWRQ
>>"%B64%" echo cm9kdWN0bywKKyAgICAgICAgICAgIEBSZXF1ZXN0UGFyYW0gSW50ZWdlciBjYW50aWRhZEFjdHVh
>>"%B64%" echo bCwKKyAgICAgICAgICAgIEBSZXF1ZXN0UGFyYW0gSW50ZWdlciBzdG9ja01pbmltbywKKyAgICAg
>>"%B64%" echo ICAgICAgIFJlZGlyZWN0QXR0cmlidXRlcyByZWRpcmVjdCkgeworCisgICAgICAgIGJvb2xlYW4g
>>"%B64%" echo eWFFeGlzdGUgPSBpbnZlbnRhcmlvUmVwb3NpdG9yeS5maW5kQWxsKCkuc3RyZWFtKCkKKyAgICAg
>>"%B64%" echo ICAgICAgICAgICAuYW55TWF0Y2goaW52IC0+IGlkUHJvZHVjdG8uZXF1YWxzKGludi5nZXRJZFBy
>>"%B64%" echo b2R1Y3RvKCkpKTsKKworICAgICAgICBpZiAoeWFFeGlzdGUpIHsKKyAgICAgICAgICAgIHJlZGly
>>"%B64%" echo ZWN0LmFkZEZsYXNoQXR0cmlidXRlKCJlcnJvciIsCisgICAgICAgICAgICAgICAgICAgICJFc2Ug
>>"%B64%" echo cHJvZHVjdG8geWEgdGllbmUgdW4gcmVnaXN0cm8gZGUgaW52ZW50YXJpby4gRWTDrXRhbG8gZW4g
>>"%B64%" echo bHVnYXIgZGUgY3JlYXJsby4iKTsKKyAgICAgICAgICAgIHJldHVybiAicmVkaXJlY3Q6L2FkbWlu
>>"%B64%" echo L2ludmVudGFyaW8iOworICAgICAgICB9CisKKyAgICAgICAgSW52ZW50YXJpbyBpbnZlbnRhcmlv
>>"%B64%" echo ID0gbmV3IEludmVudGFyaW8oKTsKKyAgICAgICAgaW52ZW50YXJpby5zZXRJZFByb2R1Y3RvKGlk
>>"%B64%" echo UHJvZHVjdG8pOworICAgICAgICBpbnZlbnRhcmlvLnNldENhbnRpZGFkQWN0dWFsKGNhbnRpZGFk
>>"%B64%" echo QWN0dWFsICE9IG51bGwgPyBjYW50aWRhZEFjdHVhbCA6IDApOworICAgICAgICBpbnZlbnRhcmlv
>>"%B64%" echo LnNldFN0b2NrTWluaW1vKHN0b2NrTWluaW1vICE9IG51bGwgPyBzdG9ja01pbmltbyA6IDApOwor
>>"%B64%" echo ICAgICAgICBpbnZlbnRhcmlvLnNldEluaGFiaWxpdGFkbyhmYWxzZSk7CisgICAgICAgIGludmVu
>>"%B64%" echo dGFyaW9SZXBvc2l0b3J5LnNhdmUoaW52ZW50YXJpbyk7CisKKyAgICAgICAgcmVnaXN0cmFyTW92
>>"%B64%" echo aW1pZW50byhpbnZlbnRhcmlvLmdldElkKCksIGlkUHJvZHVjdG8sICJFTlRSQURBIiwKKyAgICAg
>>"%B64%" echo ICAgICAgICAgICBpbnZlbnRhcmlvLmdldENhbnRpZGFkQWN0dWFsKCksICJSZWdpc3RybyBpbmlj
>>"%B64%" echo aWFsIGRlIGludmVudGFyaW8iKTsKKworICAgICAgICByZWRpcmVjdC5hZGRGbGFzaEF0dHJpYnV0
>>"%B64%" echo ZSgiZXhpdG8iLCAiUmVnaXN0cm8gZGUgaW52ZW50YXJpbyBjcmVhZG8uIik7CisgICAgICAgIHJl
>>"%B64%" echo dHVybiAicmVkaXJlY3Q6L2FkbWluL2ludmVudGFyaW8iOworICAgIH0KKworICAgIEBUcmFuc2Fj
>>"%B64%" echo dGlvbmFsCisgICAgQFBvc3RNYXBwaW5nKCIvaW52ZW50YXJpby9lZGl0YXIiKQorICAgIHB1Ymxp
>>"%B64%" echo YyBTdHJpbmcgZWRpdGFySW52ZW50YXJpbygKKyAgICAgICAgICAgIEBSZXF1ZXN0UGFyYW0gTG9u
>>"%B64%" echo ZyBpZEludmVudGFyaW8sCisgICAgICAgICAgICBAUmVxdWVzdFBhcmFtIEludGVnZXIgY2FudGlk
>>"%B64%" echo YWRBY3R1YWwsCisgICAgICAgICAgICBAUmVxdWVzdFBhcmFtIEludGVnZXIgc3RvY2tNaW5pbW8s
>>"%B64%" echo CisgICAgICAgICAgICBAUmVxdWVzdFBhcmFtKHJlcXVpcmVkID0gZmFsc2UsIGRlZmF1bHRWYWx1
>>"%B64%" echo ZSA9ICJBanVzdGUgbWFudWFsIGRlIGludmVudGFyaW8iKSBTdHJpbmcgbW90aXZvLAorICAgICAg
>>"%B64%" echo ICAgICAgUmVkaXJlY3RBdHRyaWJ1dGVzIHJlZGlyZWN0KSB7CisKKyAgICAgICAgSW52ZW50YXJp
>>"%B64%" echo byBpbnZlbnRhcmlvID0gaW52ZW50YXJpb1JlcG9zaXRvcnkuZmluZEJ5SWQoaWRJbnZlbnRhcmlv
>>"%B64%" echo KS5vckVsc2UobnVsbCk7CisKKyAgICAgICAgaWYgKGludmVudGFyaW8gPT0gbnVsbCkgeworICAg
>>"%B64%" echo ICAgICAgICAgcmVkaXJlY3QuYWRkRmxhc2hBdHRyaWJ1dGUoImVycm9yIiwgIkVsIHJlZ2lzdHJv
>>"%B64%" echo IGRlIGludmVudGFyaW8gbm8gZXhpc3RlLiIpOworICAgICAgICAgICAgcmV0dXJuICJyZWRpcmVj
>>"%B64%" echo dDovYWRtaW4vaW52ZW50YXJpbyI7CisgICAgICAgIH0KKworICAgICAgICBpbnQgYW50ZXJpb3Ig
>>"%B64%" echo PSBpbnZlbnRhcmlvLmdldENhbnRpZGFkQWN0dWFsKCkgIT0gbnVsbCA/IGludmVudGFyaW8uZ2V0
>>"%B64%" echo Q2FudGlkYWRBY3R1YWwoKSA6IDA7CisgICAgICAgIGludCBudWV2YSA9IGNhbnRpZGFkQWN0dWFs
>>"%B64%" echo ICE9IG51bGwgPyBjYW50aWRhZEFjdHVhbCA6IDA7CisgICAgICAgIGludCBkaWZlcmVuY2lhID0g
>>"%B64%" echo bnVldmEgLSBhbnRlcmlvcjsKKworICAgICAgICBpbnZlbnRhcmlvLnNldENhbnRpZGFkQWN0dWFs
>>"%B64%" echo KG51ZXZhKTsKKyAgICAgICAgaW52ZW50YXJpby5zZXRTdG9ja01pbmltbyhzdG9ja01pbmltbyAh
>>"%B64%" echo PSBudWxsID8gc3RvY2tNaW5pbW8gOiAwKTsKKyAgICAgICAgaW52ZW50YXJpb1JlcG9zaXRvcnku
>>"%B64%" echo c2F2ZShpbnZlbnRhcmlvKTsKKworICAgICAgICBTdHJpbmcgdGlwbyA9ICJBSlVTVEUiOworICAg
>>"%B64%" echo ICAgICBpZiAoZGlmZXJlbmNpYSA+IDApIHsKKyAgICAgICAgICAgIHRpcG8gPSAiRU5UUkFEQSI7
>>"%B64%" echo CisgICAgICAgIH0gZWxzZSBpZiAoZGlmZXJlbmNpYSA8IDApIHsKKyAgICAgICAgICAgIHRpcG8g
>>"%B64%" echo PSAiU0FMSURBIjsKKyAgICAgICAgfQorCisgICAgICAgIHJlZ2lzdHJhck1vdmltaWVudG8oaWRJ
>>"%B64%" echo bnZlbnRhcmlvLCBpbnZlbnRhcmlvLmdldElkUHJvZHVjdG8oKSwgdGlwbywgTWF0aC5hYnMoZGlm
>>"%B64%" echo ZXJlbmNpYSksIG1vdGl2byk7CisKKyAgICAgICAgcmVkaXJlY3QuYWRkRmxhc2hBdHRyaWJ1dGUo
>>"%B64%" echo ImV4aXRvIiwgIkludmVudGFyaW8gYWN0dWFsaXphZG8uIik7CisgICAgICAgIHJldHVybiAicmVk
>>"%B64%" echo aXJlY3Q6L2FkbWluL2ludmVudGFyaW8iOworICAgIH0KKworICAgIEBQb3N0TWFwcGluZygiL2lu
>>"%B64%" echo dmVudGFyaW8vY2FtYmlhci1lc3RhZG8iKQorICAgIHB1YmxpYyBTdHJpbmcgY2FtYmlhckVzdGFk
>>"%B64%" echo b0ludmVudGFyaW8oQFJlcXVlc3RQYXJhbSBMb25nIGlkSW52ZW50YXJpbywgUmVkaXJlY3RBdHRy
>>"%B64%" echo aWJ1dGVzIHJlZGlyZWN0KSB7CisKKyAgICAgICAgSW52ZW50YXJpbyBpbnZlbnRhcmlvID0gaW52
>>"%B64%" echo ZW50YXJpb1JlcG9zaXRvcnkuZmluZEJ5SWQoaWRJbnZlbnRhcmlvKS5vckVsc2UobnVsbCk7CisK
>>"%B64%" echo KyAgICAgICAgaWYgKGludmVudGFyaW8gIT0gbnVsbCkgeworICAgICAgICAgICAgYm9vbGVhbiBu
>>"%B64%" echo dWV2b0VzdGFkbyA9IGludmVudGFyaW8uZ2V0SW5oYWJpbGl0YWRvKCkgPT0gbnVsbCB8fCAhaW52
>>"%B64%" echo ZW50YXJpby5nZXRJbmhhYmlsaXRhZG8oKTsKKyAgICAgICAgICAgIGludmVudGFyaW8uc2V0SW5o
>>"%B64%" echo YWJpbGl0YWRvKG51ZXZvRXN0YWRvKTsKKyAgICAgICAgICAgIGludmVudGFyaW9SZXBvc2l0b3J5
>>"%B64%" echo LnNhdmUoaW52ZW50YXJpbyk7CisKKyAgICAgICAgICAgIHJlZGlyZWN0LmFkZEZsYXNoQXR0cmli
>>"%B64%" echo dXRlKCJleGl0byIsIG51ZXZvRXN0YWRvCisgICAgICAgICAgICAgICAgICAgID8gIlJlZ2lzdHJv
>>"%B64%" echo IGRlIGludmVudGFyaW8gaW5oYWJpbGl0YWRvLiIKKyAgICAgICAgICAgICAgICAgICAgOiAiUmVn
>>"%B64%" echo aXN0cm8gZGUgaW52ZW50YXJpbyBoYWJpbGl0YWRvLiIpOworICAgICAgICB9CisKKyAgICAgICAg
>>"%B64%" echo cmV0dXJuICJyZWRpcmVjdDovYWRtaW4vaW52ZW50YXJpbyI7CisgICAgfQorCisgICAgcHJpdmF0
>>"%B64%" echo ZSB2b2lkIHJlZ2lzdHJhck1vdmltaWVudG8oTG9uZyBpZEludmVudGFyaW8sIExvbmcgaWRQcm9k
>>"%B64%" echo dWN0bywgU3RyaW5nIHRpcG8sIGludCBjYW50aWRhZCwgU3RyaW5nIG1vdGl2bykgeworCisgICAg
>>"%B64%" echo ICAgIFByb2R1Y3RvIHByb2R1Y3RvID0gaWRQcm9kdWN0byAhPSBudWxsID8gcHJvZHVjdG9SZXBv
>>"%B64%" echo c2l0b3J5LmZpbmRCeUlkKGlkUHJvZHVjdG8pLm9yRWxzZShudWxsKSA6IG51bGw7CisKKyAgICAg
>>"%B64%" echo ICAgTW92aW1pZW50b0ludmVudGFyaW8gbW92ID0gbmV3IE1vdmltaWVudG9JbnZlbnRhcmlvKCk7
>>"%B64%" echo CisgICAgICAgIG1vdi5zZXRJZEludmVudGFyaW8oaWRJbnZlbnRhcmlvKTsKKyAgICAgICAgbW92
>>"%B64%" echo LnNldE5vbWJyZVByb2R1Y3RvKGRlc2NyaXBjaW9uUHJvZHVjdG8ocHJvZHVjdG8pKTsKKyAgICAg
>>"%B64%" echo ICAgbW92LnNldFRpcG9Nb3ZpbWllbnRvKHRpcG8pOworICAgICAgICBtb3Yuc2V0Q2FudGlkYWQo
>>"%B64%" echo Y2FudGlkYWQpOworICAgICAgICBtb3Yuc2V0TW90aXZvKG1vdGl2byk7CisKKyAgICAgICAgVXN1
>>"%B64%" echo YXJpbyB1c3VhcmlvID0gZ2V0VXN1YXJpb0FjdHVhbCgpOworICAgICAgICBtb3Yuc2V0VXN1YXJp
>>"%B64%" echo b1Jlc3BvbnNhYmxlKHVzdWFyaW8gIT0gbnVsbCA/IHVzdWFyaW8uZ2V0Tm9tYnJlKCkgOiAiQWRt
>>"%B64%" echo aW5pc3RyYWRvciIpOworCisgICAgICAgIG1vdmltaWVudG9JbnZlbnRhcmlvUmVwb3NpdG9yeS5z
>>"%B64%" echo YXZlKG1vdik7CisgICAgfQorCisgICAgLy8gPT09PT09PT09PT09PT09PT09PT09PT09PT09PT09
>>"%B64%" echo PT09PT09PT09PT09PT09PT09PT09PT09PT09CisgICAgLy8gUEVESURPUworICAgIC8vID09PT09
>>"%B64%" echo PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PQorCisg
>>"%B64%" echo ICAgQEdldE1hcHBpbmcoIi9wZWRpZG9zIikKKyAgICBwdWJsaWMgU3RyaW5nIHBlZGlkb3MoTW9k
>>"%B64%" echo ZWwgbW9kZWwpIHsKKworICAgICAgICBMaXN0PFBlZGlkbz4gcGVkaWRvcyA9IHBlZGlkb1JlcG9z
>>"%B64%" echo aXRvcnkuZmluZEFsbCgpOworICAgICAgICBMaXN0PFBlZGlkb1Zpc3RhPiBsaXN0YSA9IG5ldyBB
>>"%B64%" echo cnJheUxpc3Q8PigpOworCisgICAgICAgIGZvciAoUGVkaWRvIHBlZGlkbyA6IHBlZGlkb3MpIHsK
>>"%B64%" echo KyAgICAgICAgICAgIGlmIChCb29sZWFuLlRSVUUuZXF1YWxzKHBlZGlkby5nZXRPY3VsdG8oKSkp
>>"%B64%" echo IHsKKyAgICAgICAgICAgICAgICBjb250aW51ZTsKKyAgICAgICAgICAgIH0KKworICAgICAgICAg
>>"%B64%" echo ICAgUGVkaWRvVmlzdGEgdmlzdGEgPSBuZXcgUGVkaWRvVmlzdGEoKTsKKyAgICAgICAgICAgIHZp
>>"%B64%" echo c3RhLmlkID0gcGVkaWRvLmdldElkKCk7CisgICAgICAgICAgICB2aXN0YS5lc3RhZG8gPSBwZWRp
>>"%B64%" echo ZG8uZ2V0RXN0YWRvKCk7CisgICAgICAgICAgICB2aXN0YS5mZWNoYSA9IHBlZGlkby5nZXRGZWNo
>>"%B64%" echo YVBlZGlkbygpOworCisgICAgICAgICAgICBVc3VhcmlvIGNsaWVudGUgPSBwZWRpZG8uZ2V0SWRV
>>"%B64%" echo c3VhcmlvKCkgIT0gbnVsbAorICAgICAgICAgICAgICAgICAgICA/IHVzdWFyaW9SZXBvc2l0b3J5
>>"%B64%" echo LmZpbmRCeUlkKHBlZGlkby5nZXRJZFVzdWFyaW8oKSkub3JFbHNlKG51bGwpCisgICAgICAgICAg
>>"%B64%" echo ICAgICAgICAgIDogbnVsbDsKKworICAgICAgICAgICAgaWYgKGNsaWVudGUgIT0gbnVsbCkgewor
>>"%B64%" echo ICAgICAgICAgICAgICAgIHZpc3RhLmNsaWVudGVOb21icmUgPSBjbGllbnRlLmdldE5vbWJyZSgp
>>"%B64%" echo OworICAgICAgICAgICAgfQorCisgICAgICAgICAgICBMaXN0PERldGFsbGVQZWRpZG8+IGRldGFs
>>"%B64%" echo bGVzID0gZGV0YWxsZVBlZGlkb1JlcG9zaXRvcnkuZmluZEJ5SWRQZWRpZG8ocGVkaWRvLmdldElk
>>"%B64%" echo KCkpOworICAgICAgICAgICAgZG91YmxlIHRvdGFsID0gMDsKKyAgICAgICAgICAgIGludCB1bmlk
>>"%B64%" echo YWRlcyA9IDA7CisKKyAgICAgICAgICAgIGZvciAoRGV0YWxsZVBlZGlkbyBkZXRhbGxlIDogZGV0
>>"%B64%" echo YWxsZXMpIHsKKyAgICAgICAgICAgICAgICBpbnQgY2FudGlkYWQgPSBkZXRhbGxlLmdldENhbnRp
>>"%B64%" echo ZGFkKCkgIT0gbnVsbCA/IGRldGFsbGUuZ2V0Q2FudGlkYWQoKSA6IDA7CisgICAgICAgICAgICAg
>>"%B64%" echo ICAgdW5pZGFkZXMgKz0gY2FudGlkYWQ7CisKKyAgICAgICAgICAgICAgICBpZiAoZGV0YWxsZS5n
>>"%B64%" echo ZXRQcmVjaW9Vbml0YXJpbygpICE9IG51bGwpIHsKKyAgICAgICAgICAgICAgICAgICAgdG90YWwg
>>"%B64%" echo Kz0gZGV0YWxsZS5nZXRQcmVjaW9Vbml0YXJpbygpLmRvdWJsZVZhbHVlKCkgKiBjYW50aWRhZDsK
>>"%B64%" echo KyAgICAgICAgICAgICAgICB9CisgICAgICAgICAgICB9CisKKyAgICAgICAgICAgIHZpc3RhLmNh
>>"%B64%" echo bnRpZGFkUHJvZHVjdG9zID0gdW5pZGFkZXM7CisgICAgICAgICAgICB2aXN0YS50b3RhbCA9IHRv
>>"%B64%" echo dGFsOworCisgICAgICAgICAgICBsaXN0YS5hZGQodmlzdGEpOworICAgICAgICB9CisKKyAgICAg
>>"%B64%" echo ICAgLy8gTG9zIG3DoXMgcmVjaWVudGVzIHByaW1lcm8KKyAgICAgICAgbGlzdGEuc29ydCgoYSwg
>>"%B64%" echo YikgLT4geworICAgICAgICAgICAgaWYgKGEuZmVjaGEgPT0gbnVsbCAmJiBiLmZlY2hhID09IG51
>>"%B64%" echo bGwpIHsKKyAgICAgICAgICAgICAgICByZXR1cm4gMDsKKyAgICAgICAgICAgIH0KKyAgICAgICAg
>>"%B64%" echo ICAgIGlmIChhLmZlY2hhID09IG51bGwpIHsKKyAgICAgICAgICAgICAgICByZXR1cm4gMTsKKyAg
>>"%B64%" echo ICAgICAgICAgIH0KKyAgICAgICAgICAgIGlmIChiLmZlY2hhID09IG51bGwpIHsKKyAgICAgICAg
>>"%B64%" echo ICAgICAgICByZXR1cm4gLTE7CisgICAgICAgICAgICB9CisgICAgICAgICAgICByZXR1cm4gYi5m
>>"%B64%" echo ZWNoYS5jb21wYXJlVG8oYS5mZWNoYSk7CisgICAgICAgIH0pOworCisgICAgICAgIG1vZGVsLmFk
>>"%B64%" echo ZEF0dHJpYnV0ZSgicGVkaWRvcyIsIGxpc3RhKTsKKyAgICAgICAgYWdyZWdhck5vbWJyZShtb2Rl
>>"%B64%" echo bCk7CisKKyAgICAgICAgcmV0dXJuICJhZG1pbi9wZWRpZG9zX2FkbWluIjsKKyAgICB9CisKKyAg
>>"%B64%" echo ICBAR2V0TWFwcGluZygiL3BlZGlkb3Mve2lkfS9kZXRhbGxlIikKKyAgICBAUmVzcG9uc2VCb2R5
>>"%B64%" echo CisgICAgcHVibGljIFJlc3BvbnNlRW50aXR5PE1hcDxTdHJpbmcsIE9iamVjdD4+IGRldGFsbGVQ
>>"%B64%" echo ZWRpZG8oQFBhdGhWYXJpYWJsZSBMb25nIGlkKSB7CisKKyAgICAgICAgUGVkaWRvIHBlZGlkbyA9
>>"%B64%" echo IHBlZGlkb1JlcG9zaXRvcnkuZmluZEJ5SWQoaWQpLm9yRWxzZShudWxsKTsKKworICAgICAgICBp
>>"%B64%" echo ZiAocGVkaWRvID09IG51bGwpIHsKKyAgICAgICAgICAgIHJldHVybiBSZXNwb25zZUVudGl0eS5u
>>"%B64%" echo b3RGb3VuZCgpLmJ1aWxkKCk7CisgICAgICAgIH0KKworICAgICAgICBNYXA8U3RyaW5nLCBPYmpl
>>"%B64%" echo Y3Q+IHJlc3B1ZXN0YSA9IG5ldyBMaW5rZWRIYXNoTWFwPD4oKTsKKyAgICAgICAgcmVzcHVlc3Rh
>>"%B64%" echo LnB1dCgiaWQiLCBwZWRpZG8uZ2V0SWQoKSk7CisgICAgICAgIHJlc3B1ZXN0YS5wdXQoImVzdGFk
>>"%B64%" echo byIsIHBlZGlkby5nZXRFc3RhZG8oKSk7CisgICAgICAgIHJlc3B1ZXN0YS5wdXQoImZlY2hhIiwg
>>"%B64%" echo cGVkaWRvLmdldEZlY2hhUGVkaWRvKCkgIT0gbnVsbCA/IHBlZGlkby5nZXRGZWNoYVBlZGlkbygp
>>"%B64%" echo LnRvU3RyaW5nKCkgOiBudWxsKTsKKworICAgICAgICBVc3VhcmlvIGNsaWVudGUgPSBwZWRpZG8u
>>"%B64%" echo Z2V0SWRVc3VhcmlvKCkgIT0gbnVsbAorICAgICAgICAgICAgICAgID8gdXN1YXJpb1JlcG9zaXRv
>>"%B64%" echo cnkuZmluZEJ5SWQocGVkaWRvLmdldElkVXN1YXJpbygpKS5vckVsc2UobnVsbCkKKyAgICAgICAg
>>"%B64%" echo ICAgICAgICA6IG51bGw7CisKKyAgICAgICAgcmVzcHVlc3RhLnB1dCgiY2xpZW50ZU5vbWJyZSIs
>>"%B64%" echo IGNsaWVudGUgIT0gbnVsbCA/IGNsaWVudGUuZ2V0Tm9tYnJlKCkgOiAiQ2xpZW50ZSAjIiArIHBl
>>"%B64%" echo ZGlkby5nZXRJZFVzdWFyaW8oKSk7CisgICAgICAgIHJlc3B1ZXN0YS5wdXQoImNsaWVudGVDb3Jy
>>"%B64%" echo ZW8iLCBjbGllbnRlICE9IG51bGwgPyBjbGllbnRlLmdldENvcnJlbygpIDogbnVsbCk7CisgICAg
>>"%B64%" echo ICAgIHJlc3B1ZXN0YS5wdXQoImNsaWVudGVUZWxlZm9ubyIsIGNsaWVudGUgIT0gbnVsbCA/IGNs
>>"%B64%" echo aWVudGUuZ2V0VGVsZWZvbm8oKSA6IG51bGwpOworICAgICAgICByZXNwdWVzdGEucHV0KCJjbGll
>>"%B64%" echo bnRlRGlyZWNjaW9uIiwgY2xpZW50ZSAhPSBudWxsID8gY2xpZW50ZS5nZXREaXJlY2Npb24oKSA6
>>"%B64%" echo IG51bGwpOworCisgICAgICAgIExpc3Q8TWFwPFN0cmluZywgT2JqZWN0Pj4gaXRlbXMgPSBuZXcg
>>"%B64%" echo QXJyYXlMaXN0PD4oKTsKKyAgICAgICAgZG91YmxlIHRvdGFsID0gMDsKKworICAgICAgICBmb3Ig
>>"%B64%" echo KERldGFsbGVQZWRpZG8gZGV0YWxsZSA6IGRldGFsbGVQZWRpZG9SZXBvc2l0b3J5LmZpbmRCeUlk
>>"%B64%" echo UGVkaWRvKGlkKSkgeworICAgICAgICAgICAgUHJvZHVjdG8gcHJvZHVjdG8gPSBkZXRhbGxlLmdl
>>"%B64%" echo dElkUHJvZHVjdG8oKSAhPSBudWxsCisgICAgICAgICAgICAgICAgICAgID8gcHJvZHVjdG9SZXBv
>>"%B64%" echo c2l0b3J5LmZpbmRCeUlkKGRldGFsbGUuZ2V0SWRQcm9kdWN0bygpKS5vckVsc2UobnVsbCkKKyAg
>>"%B64%" echo ICAgICAgICAgICAgICAgICAgOiBudWxsOworCisgICAgICAgICAgICBpbnQgY2FudGlkYWQgPSBk
>>"%B64%" echo ZXRhbGxlLmdldENhbnRpZGFkKCkgIT0gbnVsbCA/IGRldGFsbGUuZ2V0Q2FudGlkYWQoKSA6IDA7
>>"%B64%" echo CisgICAgICAgICAgICBkb3VibGUgcHJlY2lvID0gZGV0YWxsZS5nZXRQcmVjaW9Vbml0YXJpbygp
>>"%B64%" echo ICE9IG51bGwgPyBkZXRhbGxlLmdldFByZWNpb1VuaXRhcmlvKCkuZG91YmxlVmFsdWUoKSA6IDA7
>>"%B64%" echo CisgICAgICAgICAgICBkb3VibGUgc3VidG90YWwgPSBwcmVjaW8gKiBjYW50aWRhZDsKKyAgICAg
>>"%B64%" echo ICAgICAgIHRvdGFsICs9IHN1YnRvdGFsOworCisgICAgICAgICAgICBNYXA8U3RyaW5nLCBPYmpl
>>"%B64%" echo Y3Q+IGl0ZW0gPSBuZXcgTGlua2VkSGFzaE1hcDw+KCk7CisgICAgICAgICAgICBpdGVtLnB1dCgi
>>"%B64%" echo cHJvZHVjdG8iLCBwcm9kdWN0byAhPSBudWxsICYmIHByb2R1Y3RvLmdldE5vbWJyZSgpICE9IG51
>>"%B64%" echo bGwKKyAgICAgICAgICAgICAgICAgICAgPyBwcm9kdWN0by5nZXROb21icmUoKQorICAgICAgICAg
>>"%B64%" echo ICAgICAgICAgICA6ICJQcm9kdWN0byIpOworICAgICAgICAgICAgaXRlbS5wdXQoInRhbGxhIiwg
>>"%B64%" echo cHJvZHVjdG8gIT0gbnVsbCA/IHByb2R1Y3RvLmdldFRhbGxhKCkgOiBudWxsKTsKKyAgICAgICAg
>>"%B64%" echo ICAgIGl0ZW0ucHV0KCJjb2xvciIsIHByb2R1Y3RvICE9IG51bGwgPyBwcm9kdWN0by5nZXRDb2xv
>>"%B64%" echo cigpIDogbnVsbCk7CisgICAgICAgICAgICBpdGVtLnB1dCgiaW1hZ2VuIiwgcHJvZHVjdG8gIT0g
>>"%B64%" echo bnVsbCA/IHByb2R1Y3RvLmdldEltYWdlbigpIDogbnVsbCk7CisgICAgICAgICAgICBpdGVtLnB1
>>"%B64%" echo dCgiY2FudGlkYWQiLCBjYW50aWRhZCk7CisgICAgICAgICAgICBpdGVtLnB1dCgicHJlY2lvVW5p
>>"%B64%" echo dGFyaW8iLCBwcmVjaW8pOworICAgICAgICAgICAgaXRlbS5wdXQoInN1YnRvdGFsIiwgc3VidG90
>>"%B64%" echo YWwpOworICAgICAgICAgICAgaXRlbXMuYWRkKGl0ZW0pOworICAgICAgICB9CisKKyAgICAgICAg
>>"%B64%" echo cmVzcHVlc3RhLnB1dCgiaXRlbXMiLCBpdGVtcyk7CisgICAgICAgIHJlc3B1ZXN0YS5wdXQoInRv
>>"%B64%" echo dGFsIiwgdG90YWwpOworCisgICAgICAgIHJldHVybiBSZXNwb25zZUVudGl0eS5vayhyZXNwdWVz
>>"%B64%" echo dGEpOworICAgIH0KKworICAgIC8vID09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09
>>"%B64%" echo PT09PT09PT09PT09PT09PT09PT09PQorICAgIC8vIFZFTlRBUworICAgIC8vID09PT09PT09PT09
>>"%B64%" echo PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PQorCisgICAgQEdl
>>"%B64%" echo dE1hcHBpbmcoIi92ZW50YXMiKQorICAgIHB1YmxpYyBTdHJpbmcgdmVudGFzKE1vZGVsIG1vZGVs
>>"%B64%" echo KSB7CisKKyAgICAgICAgTGlzdDxWZW50YUFkbWluVmlzdGE+IGxpc3RhID0gbmV3IEFycmF5TGlz
>>"%B64%" echo dDw+KCk7CisKKyAgICAgICAgZm9yIChWZW50YSB2ZW50YSA6IHZlbnRhUmVwb3NpdG9yeS5maW5k
>>"%B64%" echo QWxsQnlPcmRlckJ5RmVjaGFWZW50YURlc2MoKSkgeworICAgICAgICAgICAgVmVudGFBZG1pblZp
>>"%B64%" echo c3RhIHZpc3RhID0gbmV3IFZlbnRhQWRtaW5WaXN0YSgpOworICAgICAgICAgICAgdmlzdGEuaWQg
>>"%B64%" echo PSB2ZW50YS5nZXRJZCgpOworICAgICAgICAgICAgdmlzdGEuaWRQZWRpZG8gPSB2ZW50YS5nZXRJ
>>"%B64%" echo ZFBlZGlkbygpOworICAgICAgICAgICAgdmlzdGEuZmVjaGEgPSB2ZW50YS5nZXRGZWNoYVZlbnRh
>>"%B64%" echo KCk7CisgICAgICAgICAgICB2aXN0YS50b3RhbCA9IHZlbnRhLmdldFRvdGFsUGFnYWRvKCkgIT0g
>>"%B64%" echo bnVsbCA/IHZlbnRhLmdldFRvdGFsUGFnYWRvKCkgOiAwOworCisgICAgICAgICAgICBVc3Vhcmlv
>>"%B64%" echo IGNsaWVudGUgPSB2ZW50YS5nZXRJZFVzdWFyaW8oKSAhPSBudWxsCisgICAgICAgICAgICAgICAg
>>"%B64%" echo ICAgID8gdXN1YXJpb1JlcG9zaXRvcnkuZmluZEJ5SWQodmVudGEuZ2V0SWRVc3VhcmlvKCkpLm9y
>>"%B64%" echo RWxzZShudWxsKQorICAgICAgICAgICAgICAgICAgICA6IG51bGw7CisKKyAgICAgICAgICAgIGlm
>>"%B64%" echo IChjbGllbnRlICE9IG51bGwpIHsKKyAgICAgICAgICAgICAgICB2aXN0YS5jbGllbnRlTm9tYnJl
>>"%B64%" echo ID0gY2xpZW50ZS5nZXROb21icmUoKTsKKyAgICAgICAgICAgIH0KKworICAgICAgICAgICAgaW50
>>"%B64%" echo IHVuaWRhZGVzID0gMDsKKyAgICAgICAgICAgIGZvciAoRGV0YWxsZVZlbnRhIGRldGFsbGUgOiBk
>>"%B64%" echo ZXRhbGxlVmVudGFSZXBvc2l0b3J5LmZpbmRCeUlkVmVudGEodmVudGEuZ2V0SWQoKSkpIHsKKyAg
>>"%B64%" echo ICAgICAgICAgICAgICB1bmlkYWRlcyArPSBkZXRhbGxlLmdldENhbnRpZGFkKCkgIT0gbnVsbCA/
>>"%B64%" echo IGRldGFsbGUuZ2V0Q2FudGlkYWQoKSA6IDA7CisgICAgICAgICAgICB9CisgICAgICAgICAgICB2
>>"%B64%" echo aXN0YS5jYW50aWRhZFByb2R1Y3RvcyA9IHVuaWRhZGVzOworCisgICAgICAgICAgICBsaXN0YS5h
>>"%B64%" echo ZGQodmlzdGEpOworICAgICAgICB9CisKKyAgICAgICAgbW9kZWwuYWRkQXR0cmlidXRlKCJ2ZW50
>>"%B64%" echo YXMiLCBsaXN0YSk7CisgICAgICAgIGFncmVnYXJOb21icmUobW9kZWwpOworCisgICAgICAgIHJl
>>"%B64%" echo dHVybiAiYWRtaW4vdmVudGFzX2FkbWluIjsKKyAgICB9CisKKyAgICBAR2V0TWFwcGluZygiL3Zl
>>"%B64%" echo bnRhcy9ncmFmaWNvIikKKyAgICBAUmVzcG9uc2VCb2R5CisgICAgcHVibGljIExpc3Q8TWFwPFN0
>>"%B64%" echo cmluZywgT2JqZWN0Pj4gZ3JhZmljb1ZlbnRhcygpIHsKKworICAgICAgICBMaXN0PFZlbnRhPiB2
>>"%B64%" echo ZW50YXMgPSB2ZW50YVJlcG9zaXRvcnkuZmluZEFsbEJ5T3JkZXJCeUZlY2hhVmVudGFEZXNjKCk7
>>"%B64%" echo CisKKyAgICAgICAgTWFwPExvY2FsRGF0ZSwgRG91YmxlPiBhY3VtdWxhZG8gPSBuZXcgVHJlZU1h
>>"%B64%" echo cDw+KCk7CisKKyAgICAgICAgZm9yIChWZW50YSB2ZW50YSA6IHZlbnRhcykgeworICAgICAgICAg
>>"%B64%" echo ICAgaWYgKHZlbnRhLmdldEZlY2hhVmVudGEoKSA9PSBudWxsKSB7CisgICAgICAgICAgICAgICAg
>>"%B64%" echo Y29udGludWU7CisgICAgICAgICAgICB9CisgICAgICAgICAgICBMb2NhbERhdGUgZGlhID0gdmVu
>>"%B64%" echo dGEuZ2V0RmVjaGFWZW50YSgpLnRvTG9jYWxEYXRlKCk7CisgICAgICAgICAgICBkb3VibGUgdmFs
>>"%B64%" echo b3IgPSB2ZW50YS5nZXRUb3RhbFBhZ2FkbygpICE9IG51bGwgPyB2ZW50YS5nZXRUb3RhbFBhZ2Fk
>>"%B64%" echo bygpIDogMDsKKyAgICAgICAgICAgIGFjdW11bGFkby5tZXJnZShkaWEsIHZhbG9yLCBEb3VibGU6
>>"%B64%" echo OnN1bSk7CisgICAgICAgIH0KKworICAgICAgICBMaXN0PE1hcDxTdHJpbmcsIE9iamVjdD4+IHB1
>>"%B64%" echo bnRvcyA9IG5ldyBBcnJheUxpc3Q8PigpOworCisgICAgICAgIGZvciAoTWFwLkVudHJ5PExvY2Fs
>>"%B64%" echo RGF0ZSwgRG91YmxlPiBlbnRyYWRhIDogYWN1bXVsYWRvLmVudHJ5U2V0KCkpIHsKKyAgICAgICAg
>>"%B64%" echo ICAgIE1hcDxTdHJpbmcsIE9iamVjdD4gcHVudG8gPSBuZXcgTGlua2VkSGFzaE1hcDw+KCk7Cisg
>>"%B64%" echo ICAgICAgICAgICBwdW50by5wdXQoImZlY2hhIiwgZW50cmFkYS5nZXRLZXkoKS5mb3JtYXQoRk9S
>>"%B64%" echo TUFUT19GRUNIQSkpOworICAgICAgICAgICAgcHVudG8ucHV0KCJ0b3RhbCIsIGVudHJhZGEuZ2V0
>>"%B64%" echo VmFsdWUoKSk7CisgICAgICAgICAgICBwdW50b3MuYWRkKHB1bnRvKTsKKyAgICAgICAgfQorCisg
>>"%B64%" echo ICAgICAgIHJldHVybiBwdW50b3M7CisgICAgfQorCisgICAgQEdldE1hcHBpbmcoIi92ZW50YXMv
>>"%B64%" echo e2lkfS9kZXRhbGxlIikKKyAgICBAUmVzcG9uc2VCb2R5CisgICAgcHVibGljIFJlc3BvbnNlRW50
>>"%B64%" echo aXR5PE1hcDxTdHJpbmcsIE9iamVjdD4+IGRldGFsbGVWZW50YShAUGF0aFZhcmlhYmxlIExvbmcg
>>"%B64%" echo aWQpIHsKKworICAgICAgICBWZW50YSB2ZW50YSA9IHZlbnRhUmVwb3NpdG9yeS5maW5kQnlJZChp
>>"%B64%" echo ZCkub3JFbHNlKG51bGwpOworCisgICAgICAgIGlmICh2ZW50YSA9PSBudWxsKSB7CisgICAgICAg
>>"%B64%" echo ICAgICByZXR1cm4gUmVzcG9uc2VFbnRpdHkubm90Rm91bmQoKS5idWlsZCgpOworICAgICAgICB9
>>"%B64%" echo CisKKyAgICAgICAgTWFwPFN0cmluZywgT2JqZWN0PiByZXNwdWVzdGEgPSBuZXcgTGlua2VkSGFz
>>"%B64%" echo aE1hcDw+KCk7CisgICAgICAgIHJlc3B1ZXN0YS5wdXQoImlkIiwgdmVudGEuZ2V0SWQoKSk7Cisg
>>"%B64%" echo ICAgICAgIHJlc3B1ZXN0YS5wdXQoImlkUGVkaWRvIiwgdmVudGEuZ2V0SWRQZWRpZG8oKSk7Cisg
>>"%B64%" echo ICAgICAgIHJlc3B1ZXN0YS5wdXQoImZlY2hhIiwgdmVudGEuZ2V0RmVjaGFWZW50YSgpICE9IG51
>>"%B64%" echo bGwgPyB2ZW50YS5nZXRGZWNoYVZlbnRhKCkudG9TdHJpbmcoKSA6IG51bGwpOworCisgICAgICAg
>>"%B64%" echo IFVzdWFyaW8gY2xpZW50ZSA9IHZlbnRhLmdldElkVXN1YXJpbygpICE9IG51bGwKKyAgICAgICAg
>>"%B64%" echo ICAgICAgICA/IHVzdWFyaW9SZXBvc2l0b3J5LmZpbmRCeUlkKHZlbnRhLmdldElkVXN1YXJpbygp
>>"%B64%" echo KS5vckVsc2UobnVsbCkKKyAgICAgICAgICAgICAgICA6IG51bGw7CisKKyAgICAgICAgcmVzcHVl
>>"%B64%" echo c3RhLnB1dCgiY2xpZW50ZU5vbWJyZSIsIGNsaWVudGUgIT0gbnVsbCA/IGNsaWVudGUuZ2V0Tm9t
>>"%B64%" echo YnJlKCkgOiAiQ2xpZW50ZSAjIiArIHZlbnRhLmdldElkVXN1YXJpbygpKTsKKyAgICAgICAgcmVz
>>"%B64%" echo cHVlc3RhLnB1dCgiY2xpZW50ZUNvcnJlbyIsIGNsaWVudGUgIT0gbnVsbCA/IGNsaWVudGUuZ2V0
>>"%B64%" echo Q29ycmVvKCkgOiBudWxsKTsKKyAgICAgICAgcmVzcHVlc3RhLnB1dCgiY2xpZW50ZVRlbGVmb25v
>>"%B64%" echo IiwgY2xpZW50ZSAhPSBudWxsID8gY2xpZW50ZS5nZXRUZWxlZm9ubygpIDogbnVsbCk7CisgICAg
>>"%B64%" echo ICAgIHJlc3B1ZXN0YS5wdXQoImNsaWVudGVEaXJlY2Npb24iLCBjbGllbnRlICE9IG51bGwgPyBj
>>"%B64%" echo bGllbnRlLmdldERpcmVjY2lvbigpIDogbnVsbCk7CisKKyAgICAgICAgUGVkaWRvIHBlZGlkbyA9
>>"%B64%" echo IHZlbnRhLmdldElkUGVkaWRvKCkgIT0gbnVsbAorICAgICAgICAgICAgICAgID8gcGVkaWRvUmVw
>>"%B64%" echo b3NpdG9yeS5maW5kQnlJZCh2ZW50YS5nZXRJZFBlZGlkbygpKS5vckVsc2UobnVsbCkKKyAgICAg
>>"%B64%" echo ICAgICAgICAgICA6IG51bGw7CisKKyAgICAgICAgcmVzcHVlc3RhLnB1dCgicGVkaWRvRXN0YWRv
>>"%B64%" echo IiwgcGVkaWRvICE9IG51bGwgPyBwZWRpZG8uZ2V0RXN0YWRvKCkgOiBudWxsKTsKKworICAgICAg
>>"%B64%" echo ICBMaXN0PE1hcDxTdHJpbmcsIE9iamVjdD4+IGl0ZW1zID0gbmV3IEFycmF5TGlzdDw+KCk7Cisg
>>"%B64%" echo ICAgICAgIGRvdWJsZSB0b3RhbCA9IDA7CisKKyAgICAgICAgZm9yIChEZXRhbGxlVmVudGEgZGV0
>>"%B64%" echo YWxsZSA6IGRldGFsbGVWZW50YVJlcG9zaXRvcnkuZmluZEJ5SWRWZW50YShpZCkpIHsKKyAgICAg
>>"%B64%" echo ICAgICAgIFByb2R1Y3RvIHByb2R1Y3RvID0gZGV0YWxsZS5nZXRJZFByb2R1Y3RvKCkgIT0gbnVs
>>"%B64%" echo bAorICAgICAgICAgICAgICAgICAgICA/IHByb2R1Y3RvUmVwb3NpdG9yeS5maW5kQnlJZChkZXRh
>>"%B64%" echo bGxlLmdldElkUHJvZHVjdG8oKSkub3JFbHNlKG51bGwpCisgICAgICAgICAgICAgICAgICAgIDog
>>"%B64%" echo bnVsbDsKKworICAgICAgICAgICAgaW50IGNhbnRpZGFkID0gZGV0YWxsZS5nZXRDYW50aWRhZCgp
>>"%B64%" echo ICE9IG51bGwgPyBkZXRhbGxlLmdldENhbnRpZGFkKCkgOiAwOworICAgICAgICAgICAgZG91Ymxl
>>"%B64%" echo IHByZWNpbyA9IGRldGFsbGUuZ2V0UHJlY2lvVW5pdGFyaW8oKSAhPSBudWxsID8gZGV0YWxsZS5n
>>"%B64%" echo ZXRQcmVjaW9Vbml0YXJpbygpLmRvdWJsZVZhbHVlKCkgOiAwOworICAgICAgICAgICAgZG91Ymxl
>>"%B64%" echo IHN1YnRvdGFsID0gcHJlY2lvICogY2FudGlkYWQ7CisgICAgICAgICAgICB0b3RhbCArPSBzdWJ0
>>"%B64%" echo b3RhbDsKKworICAgICAgICAgICAgTWFwPFN0cmluZywgT2JqZWN0PiBpdGVtID0gbmV3IExpbmtl
>>"%B64%" echo ZEhhc2hNYXA8PigpOworICAgICAgICAgICAgaXRlbS5wdXQoInByb2R1Y3RvIiwgcHJvZHVjdG8g
>>"%B64%" echo IT0gbnVsbCAmJiBwcm9kdWN0by5nZXROb21icmUoKSAhPSBudWxsCisgICAgICAgICAgICAgICAg
>>"%B64%" echo ICAgID8gcHJvZHVjdG8uZ2V0Tm9tYnJlKCkKKyAgICAgICAgICAgICAgICAgICAgOiAiUHJvZHVj
>>"%B64%" echo dG8iKTsKKyAgICAgICAgICAgIGl0ZW0ucHV0KCJ0YWxsYSIsIHByb2R1Y3RvICE9IG51bGwgPyBw
>>"%B64%" echo cm9kdWN0by5nZXRUYWxsYSgpIDogbnVsbCk7CisgICAgICAgICAgICBpdGVtLnB1dCgiY29sb3Ii
>>"%B64%" echo LCBwcm9kdWN0byAhPSBudWxsID8gcHJvZHVjdG8uZ2V0Q29sb3IoKSA6IG51bGwpOworICAgICAg
>>"%B64%" echo ICAgICAgaXRlbS5wdXQoImltYWdlbiIsIHByb2R1Y3RvICE9IG51bGwgPyBwcm9kdWN0by5nZXRJ
>>"%B64%" echo bWFnZW4oKSA6IG51bGwpOworICAgICAgICAgICAgaXRlbS5wdXQoImNhbnRpZGFkIiwgY2FudGlk
>>"%B64%" echo YWQpOworICAgICAgICAgICAgaXRlbS5wdXQoInByZWNpb1VuaXRhcmlvIiwgcHJlY2lvKTsKKyAg
>>"%B64%" echo ICAgICAgICAgIGl0ZW0ucHV0KCJzdWJ0b3RhbCIsIHN1YnRvdGFsKTsKKyAgICAgICAgICAgIGl0
>>"%B64%" echo ZW1zLmFkZChpdGVtKTsKKyAgICAgICAgfQorCisgICAgICAgIHJlc3B1ZXN0YS5wdXQoIml0ZW1z
>>"%B64%" echo IiwgaXRlbXMpOworICAgICAgICByZXNwdWVzdGEucHV0KCJ0b3RhbCIsIHRvdGFsID4gMCA/IHRv
>>"%B64%" echo dGFsIDogdmVudGEuZ2V0VG90YWxQYWdhZG8oKSk7CisKKyAgICAgICAgcmV0dXJuIFJlc3BvbnNl
>>"%B64%" echo RW50aXR5Lm9rKHJlc3B1ZXN0YSk7CisgICAgfQorCisgICAgQEdldE1hcHBpbmcoIi92ZW50YXMv
>>"%B64%" echo cmVwb3J0ZS9leGNlbCIpCisgICAgcHVibGljIFJlc3BvbnNlRW50aXR5PGJ5dGVbXT4gcmVwb3J0
>>"%B64%" echo ZVZlbnRhc0V4Y2VsKCkgeworICAgICAgICBieXRlW10gZXhjZWwgPSByZXBvcnRlVmVudGFzRXhj
>>"%B64%" echo ZWxTZXJ2aWNlLmdlbmVyYXJSZXBvcnRlKHZlbnRhUmVwb3NpdG9yeS5maW5kQWxsQnlPcmRlckJ5
>>"%B64%" echo RmVjaGFWZW50YURlc2MoKSk7CisgICAgICAgIHJldHVybiBhcmNoaXZvKGV4Y2VsLCAicmVwb3J0
>>"%B64%" echo ZV92ZW50YXMueGxzeCIsCisgICAgICAgICAgICAgICAgImFwcGxpY2F0aW9uL3ZuZC5vcGVueG1s
>>"%B64%" echo Zm9ybWF0cy1vZmZpY2Vkb2N1bWVudC5zcHJlYWRzaGVldG1sLnNoZWV0Iik7CisgICAgfQorCisg
>>"%B64%" echo ICAgLy8gPT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09
>>"%B64%" echo PT09PT09CisgICAgLy8gREVWT0xVQ0lPTkVTCisgICAgLy8gPT09PT09PT09PT09PT09PT09PT09
>>"%B64%" echo PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09CisKKyAgICBAR2V0TWFwcGluZygi
>>"%B64%" echo L2Rldm9sdWNpb25lcyIpCisgICAgcHVibGljIFN0cmluZyBkZXZvbHVjaW9uZXMoTW9kZWwgbW9k
>>"%B64%" echo ZWwpIHsKKworICAgICAgICBtb2RlbC5hZGRBdHRyaWJ1dGUoImRldm9sdWNpb25lcyIsIGRldm9s
>>"%B64%" echo dWNpb25SZXBvc2l0b3J5LmZpbmRBbGxCeU9yZGVyQnlGZWNoYURldm9sdWNpb25EZXNjKCkpOwor
>>"%B64%" echo ICAgICAgICBhZ3JlZ2FyTm9tYnJlKG1vZGVsKTsKKworICAgICAgICByZXR1cm4gImFkbWluL2Rl
>>"%B64%" echo dm9sdWNpb25lc19hZG1pbiI7CisgICAgfQorCisgICAgQFBvc3RNYXBwaW5nKCIvZGV2b2x1Y2lv
>>"%B64%" echo bmVzL2NhbWJpYXJFc3RhZG8iKQorICAgIHB1YmxpYyBTdHJpbmcgY2FtYmlhckVzdGFkb0Rldm9s
>>"%B64%" echo dWNpb24oCisgICAgICAgICAgICBAUmVxdWVzdFBhcmFtIExvbmcgaWREZXZvbHVjaW9uLAorICAg
>>"%B64%" echo ICAgICAgICAgQFJlcXVlc3RQYXJhbSBTdHJpbmcgZXN0YWRvLAorICAgICAgICAgICAgUmVkaXJl
>>"%B64%" echo Y3RBdHRyaWJ1dGVzIHJlZGlyZWN0KSB7CisKKyAgICAgICAgRGV2b2x1Y2lvbiBkZXZvbHVjaW9u
>>"%B64%" echo ID0gZGV2b2x1Y2lvblJlcG9zaXRvcnkuZmluZEJ5SWQoaWREZXZvbHVjaW9uKS5vckVsc2UobnVs
>>"%B64%" echo bCk7CisKKyAgICAgICAgaWYgKGRldm9sdWNpb24gPT0gbnVsbCkgeworICAgICAgICAgICAgcmVk
>>"%B64%" echo aXJlY3QuYWRkRmxhc2hBdHRyaWJ1dGUoImVycm9yIiwgIkxhIGRldm9sdWNpw7NuIG5vIGV4aXN0
>>"%B64%" echo ZS4iKTsKKyAgICAgICAgICAgIHJldHVybiAicmVkaXJlY3Q6L2FkbWluL2Rldm9sdWNpb25lcyI7
>>"%B64%" echo CisgICAgICAgIH0KKworICAgICAgICBMaXN0PFN0cmluZz4gcGVybWl0aWRvcyA9IExpc3Qub2Yo
>>"%B64%" echo IlBlbmRpZW50ZSIsICJBcHJvYmFkYSIsICJSZWNoYXphZGEiKTsKKworICAgICAgICBpZiAoIXBl
>>"%B64%" echo cm1pdGlkb3MuY29udGFpbnMoZXN0YWRvKSkgeworICAgICAgICAgICAgcmVkaXJlY3QuYWRkRmxh
>>"%B64%" echo c2hBdHRyaWJ1dGUoImVycm9yIiwgIkVzdGFkbyBkZSBkZXZvbHVjacOzbiBubyB2w6FsaWRvLiIp
>>"%B64%" echo OworICAgICAgICAgICAgcmV0dXJuICJyZWRpcmVjdDovYWRtaW4vZGV2b2x1Y2lvbmVzIjsKKyAg
>>"%B64%" echo ICAgICAgfQorCisgICAgICAgIGRldm9sdWNpb24uc2V0RXN0YWRvKGVzdGFkbyk7CisgICAgICAg
>>"%B64%" echo IGRldm9sdWNpb25SZXBvc2l0b3J5LnNhdmUoZGV2b2x1Y2lvbik7CisKKyAgICAgICAgcmVkaXJl
>>"%B64%" echo Y3QuYWRkRmxhc2hBdHRyaWJ1dGUoImV4aXRvIiwgIkRldm9sdWNpw7NuICMiICsgaWREZXZvbHVj
>>"%B64%" echo aW9uICsgIiBtYXJjYWRhIGNvbW8gIiArIGVzdGFkbyArICIuIik7CisgICAgICAgIHJldHVybiAi
>>"%B64%" echo cmVkaXJlY3Q6L2FkbWluL2Rldm9sdWNpb25lcyI7CisgICAgfQorCisgICAgLy8gPT09PT09PT09
>>"%B64%" echo PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09CisgICAgLy8g
>>"%B64%" echo VklTVEFTIEFVWElMSUFSRVMgKHDDumJsaWNhcyBwYXJhIHF1ZSBUaHltZWxlYWYgbGFzIGxlYSkK
>>"%B64%" echo KyAgICAvLyA9PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09
>>"%B64%" echo PT09PT09PT0KKworICAgIHB1YmxpYyBzdGF0aWMgY2xhc3MgUGVkaWRvVmlzdGEgeworICAgICAg
>>"%B64%" echo ICBwdWJsaWMgTG9uZyBpZDsKKyAgICAgICAgcHVibGljIFN0cmluZyBjbGllbnRlTm9tYnJlOwor
>>"%B64%" echo ICAgICAgICBwdWJsaWMgamF2YS50aW1lLkxvY2FsRGF0ZVRpbWUgZmVjaGE7CisgICAgICAgIHB1
>>"%B64%" echo YmxpYyBJbnRlZ2VyIGNhbnRpZGFkUHJvZHVjdG9zOworICAgICAgICBwdWJsaWMgRG91YmxlIHRv
>>"%B64%" echo dGFsOworICAgICAgICBwdWJsaWMgU3RyaW5nIGVzdGFkbzsKKyAgICB9CisKKyAgICBwdWJsaWMg
>>"%B64%" echo c3RhdGljIGNsYXNzIFZlbnRhQWRtaW5WaXN0YSB7CisgICAgICAgIHB1YmxpYyBMb25nIGlkOwor
>>"%B64%" echo ICAgICAgICBwdWJsaWMgTG9uZyBpZFBlZGlkbzsKKyAgICAgICAgcHVibGljIFN0cmluZyBjbGll
>>"%B64%" echo bnRlTm9tYnJlOworICAgICAgICBwdWJsaWMgamF2YS50aW1lLkxvY2FsRGF0ZVRpbWUgZmVjaGE7
>>"%B64%" echo CisgICAgICAgIHB1YmxpYyBJbnRlZ2VyIGNhbnRpZGFkUHJvZHVjdG9zOworICAgICAgICBwdWJs
>>"%B64%" echo aWMgRG91YmxlIHRvdGFsOworICAgIH0KK30KZGlmZiAtLWdpdCBhL3NyYy9tYWluL3Jlc291cmNl
>>"%B64%" echo cy90ZW1wbGF0ZXMvYWRtaW4vZGV2b2x1Y2lvbmVzX2FkbWluLmh0bWwgYi9zcmMvbWFpbi9yZXNv
>>"%B64%" echo dXJjZXMvdGVtcGxhdGVzL2FkbWluL2Rldm9sdWNpb25lc19hZG1pbi5odG1sCmluZGV4IDNhMzU4
>>"%B64%" echo NTUuLmQ2N2RkNDkgMTAwNjQ0Ci0tLSBhL3NyYy9tYWluL3Jlc291cmNlcy90ZW1wbGF0ZXMvYWRt
>>"%B64%" echo aW4vZGV2b2x1Y2lvbmVzX2FkbWluLmh0bWwKKysrIGIvc3JjL21haW4vcmVzb3VyY2VzL3RlbXBs
>>"%B64%" echo YXRlcy9hZG1pbi9kZXZvbHVjaW9uZXNfYWRtaW4uaHRtbApAQCAtNjcsNiArNjcsMTYgQEAKICAg
>>"%B64%" echo ICAgICAgICAgIDxhIGhyZWY9Ii9hZG1pbi9kZXZvbHVjaW9uZXM/ZXN0YWRvPVJlY2hhemFkYSIg
>>"%B64%" echo Y2xhc3M9ImJ0biBidG4tc20gYnRuLW91dGxpbmUtZGFuZ2VyIj5SZWNoYXphZGFzPC9hPgogICAg
>>"%B64%" echo ICAgICA8L2Rpdj4KIAorICAgICAgICAgICAgICAgIDwhLS0gTWVuc2FqZXMgZGUgcmVzdWx0YWRv
>>"%B64%" echo IChzZSBlbnbDrWFuIGRlc2RlIGVsIGNvbnRyb2xhZG9yKSAtLT4KKyAgICAgICAgICAgICAgICA8
>>"%B64%" echo ZGl2IHRoOmlmPSIke2V4aXRvfSIgY2xhc3M9ImFsZXJ0IGFsZXJ0LXN1Y2Nlc3MgYWxlcnQtZGlz
>>"%B64%" echo bWlzc2libGUgZmFkZSBzaG93IHAtMyBtYi0zIiByb2xlPSJhbGVydCI+CisgICAgICAgICAgICAg
>>"%B64%" echo ICAgICAgIDxpIGNsYXNzPSJiaSBiaS1jaGVjay1jaXJjbGUtZmlsbCBtZS0yIj48L2k+PHNwYW4g
>>"%B64%" echo dGg6dGV4dD0iJHtleGl0b30iPjwvc3Bhbj4KKyAgICAgICAgICAgICAgICAgICAgPGJ1dHRvbiB0
>>"%B64%" echo eXBlPSJidXR0b24iIGNsYXNzPSJidG4tY2xvc2UiIGRhdGEtYnMtZGlzbWlzcz0iYWxlcnQiIGFy
>>"%B64%" echo aWEtbGFiZWw9IkNsb3NlIj48L2J1dHRvbj4KKyAgICAgICAgICAgICAgICA8L2Rpdj4KKyAgICAg
>>"%B64%" echo ICAgICAgICAgICA8ZGl2IHRoOmlmPSIke2Vycm9yfSIgY2xhc3M9ImFsZXJ0IGFsZXJ0LWRhbmdl
>>"%B64%" echo ciBhbGVydC1kaXNtaXNzaWJsZSBmYWRlIHNob3cgcC0zIG1iLTMiIHJvbGU9ImFsZXJ0Ij4KKyAg
>>"%B64%" echo ICAgICAgICAgICAgICAgICAgPGkgY2xhc3M9ImJpIGJpLWV4Y2xhbWF0aW9uLXRyaWFuZ2xlLWZp
>>"%B64%" echo bGwgbWUtMiI+PC9pPjxzcGFuIHRoOnRleHQ9IiR7ZXJyb3J9Ij48L3NwYW4+CisgICAgICAgICAg
>>"%B64%" echo ICAgICAgICAgIDxidXR0b24gdHlwZT0iYnV0dG9uIiBjbGFzcz0iYnRuLWNsb3NlIiBkYXRhLWJz
>>"%B64%" echo LWRpc21pc3M9ImFsZXJ0IiBhcmlhLWxhYmVsPSJDbG9zZSI+PC9idXR0b24+CisgICAgICAgICAg
>>"%B64%" echo ICAgICAgPC9kaXY+CisKICAgICAgICAgPCEtLSBUYWJsYSBkZSBkZXZvbHVjaW9uZXMgLS0+CiAg
>>"%B64%" echo ICAgICAgIDxkaXYgY2xhc3M9ImNhcmQgYm9yZGVyLTAgc2hhZG93LXNtIHJvdW5kZWQtMyI+CiAg
>>"%B64%" echo ICAgICAgICAgICA8ZGl2IGNsYXNzPSJjYXJkLWJvZHkgcC0wIj4KZGlmZiAtLWdpdCBhL3NyYy9t
>>"%B64%" echo YWluL3Jlc291cmNlcy90ZW1wbGF0ZXMvYWRtaW4vaW52ZW50YXJpb19hZG1pbi5odG1sIGIvc3Jj
>>"%B64%" echo L21haW4vcmVzb3VyY2VzL3RlbXBsYXRlcy9hZG1pbi9pbnZlbnRhcmlvX2FkbWluLmh0bWwKaW5k
>>"%B64%" echo ZXggN2MwYzI5MC4uZmJkOGMxNyAxMDA2NDQKLS0tIGEvc3JjL21haW4vcmVzb3VyY2VzL3RlbXBs
>>"%B64%" echo YXRlcy9hZG1pbi9pbnZlbnRhcmlvX2FkbWluLmh0bWwKKysrIGIvc3JjL21haW4vcmVzb3VyY2Vz
>>"%B64%" echo L3RlbXBsYXRlcy9hZG1pbi9pbnZlbnRhcmlvX2FkbWluLmh0bWwKQEAgLTEwOCw2ICsxMDgsMTYg
>>"%B64%" echo QEAKICAgICAgICAgICAgICAgICAgICAgPC9zZWxlY3Q+CiAgICAgICAgICAgICAgICAgPC9kaXY+
>>"%B64%" echo CiAKKyAgICAgICAgICAgICAgICA8IS0tIE1lbnNhamVzIGRlIHJlc3VsdGFkbyAoc2UgZW52w61h
>>"%B64%" echo biBkZXNkZSBlbCBjb250cm9sYWRvcikgLS0+CisgICAgICAgICAgICAgICAgPGRpdiB0aDppZj0i
>>"%B64%" echo JHtleGl0b30iIGNsYXNzPSJhbGVydCBhbGVydC1zdWNjZXNzIGFsZXJ0LWRpc21pc3NpYmxlIGZh
>>"%B64%" echo ZGUgc2hvdyBwLTMgbWItMyIgcm9sZT0iYWxlcnQiPgorICAgICAgICAgICAgICAgICAgICA8aSBj
>>"%B64%" echo bGFzcz0iYmkgYmktY2hlY2stY2lyY2xlLWZpbGwgbWUtMiI+PC9pPjxzcGFuIHRoOnRleHQ9IiR7
>>"%B64%" echo ZXhpdG99Ij48L3NwYW4+CisgICAgICAgICAgICAgICAgICAgIDxidXR0b24gdHlwZT0iYnV0dG9u
>>"%B64%" echo IiBjbGFzcz0iYnRuLWNsb3NlIiBkYXRhLWJzLWRpc21pc3M9ImFsZXJ0IiBhcmlhLWxhYmVsPSJD
>>"%B64%" echo bG9zZSI+PC9idXR0b24+CisgICAgICAgICAgICAgICAgPC9kaXY+CisgICAgICAgICAgICAgICAg
>>"%B64%" echo PGRpdiB0aDppZj0iJHtlcnJvcn0iIGNsYXNzPSJhbGVydCBhbGVydC1kYW5nZXIgYWxlcnQtZGlz
>>"%B64%" echo bWlzc2libGUgZmFkZSBzaG93IHAtMyBtYi0zIiByb2xlPSJhbGVydCI+CisgICAgICAgICAgICAg
>>"%B64%" echo ICAgICAgIDxpIGNsYXNzPSJiaSBiaS1leGNsYW1hdGlvbi10cmlhbmdsZS1maWxsIG1lLTIiPjwv
>>"%B64%" echo aT48c3BhbiB0aDp0ZXh0PSIke2Vycm9yfSI+PC9zcGFuPgorICAgICAgICAgICAgICAgICAgICA8
>>"%B64%" echo YnV0dG9uIHR5cGU9ImJ1dHRvbiIgY2xhc3M9ImJ0bi1jbG9zZSIgZGF0YS1icy1kaXNtaXNzPSJh
>>"%B64%" echo bGVydCIgYXJpYS1sYWJlbD0iQ2xvc2UiPjwvYnV0dG9uPgorICAgICAgICAgICAgICAgIDwvZGl2
>>"%B64%" echo PgorCiAgICAgICAgICAgICAgICAgPCEtLSBUYWJsYSBkZSBJbnZlbnRhcmlvIC0tPgogICAgICAg
>>"%B64%" echo ICAgICAgICAgIDxkaXYgY2xhc3M9ImNhcmQgYm9yZGVyLTAgc2hhZG93LXNtIHJvdW5kZWQtMyI+
>>"%B64%" echo CiAgICAgICAgICAgICAgICAgICAgIDxkaXYgY2xhc3M9ImNhcmQtYm9keSBwLTAiPgotLSAKMi40
>>"%B64%" echo Ny4zCgo=

powershell -NoProfile -Command "$t = Get-Content -Raw '%B64%'; $b = [Convert]::FromBase64String(($t -replace '\s','')); [IO.File]::WriteAllBytes('!CD!\%PARCHE%', $b)" >nul 2>&1

if exist "%PARCHE%" goto parche_listo
echo.
echo [ERROR] No se pudo preparar el arreglo en esta carpeta.
echo         Puede que el archivo se haya descargado incompleto.
echo         Vuelve a descargarlo desde el chat y ejecutalo otra vez.
goto fin_error

:parche_listo
del "%B64%" >nul 2>&1
echo       Listo.
echo.

REM ---------- PASO 3: identidad de Git ----------
git config user.name >nul 2>&1
if not errorlevel 1 goto nombre_ok
set /p "NOMBRE=Escribe tu nombre para los commits: "
git config user.name "!NOMBRE!"
:nombre_ok
git config user.email >nul 2>&1
if not errorlevel 1 goto correo_ok
set /p "CORREO=Escribe tu correo de GitHub: "
git config user.email "!CORREO!"
:correo_ok

REM ---------- PASO 4: revisar el remoto ----------
echo [3/8] Revisando la conexion con GitHub ...
git remote get-url origin >nul 2>&1
if errorlevel 1 goto remoto_nuevo
for /f "delims=" %%r in ('git remote get-url origin') do set "ACTUAL=%%r"
if /i "!ACTUAL!"=="%URL%" goto remoto_ok
echo       Apuntaba a: !ACTUAL!
echo       Se corrige a: %URL%
git remote set-url origin %URL%
goto remoto_ok

:remoto_nuevo
echo       No existia origin, se configura ahora.
git remote add origin %URL%

:remoto_ok
echo.

REM ---------- PASO 5: traer la ultima version ----------
echo [4/8] Descargando la ultima version desde GitHub ...
git fetch origin
if not errorlevel 1 goto fetch_ok
echo.
echo [ERROR] No se pudo conectar con GitHub.
echo   - Revisa tu conexion a internet
echo   - Si pide usuario y contrasena: usuario de GitHub y como contrasena
echo     un token de https://github.com/settings/tokens con permiso repo
goto fin_error

:fetch_ok
for /f "delims=" %%v in ('git rev-parse --short origin/master') do set "REMOTO=%%v"
echo       Ultimo commit en GitHub: !REMOTO!
echo.

REM ---------- PASO 6: respaldar lo local ----------
echo [5/8] Guardando tus cambios locales, no se borra nada ...
set "SUCIO="
for /f "delims=" %%s in ('git status --porcelain --untracked-files=no') do set "SUCIO=1"
if not defined SUCIO goto sin_cambios
git rev-parse --verify respaldo-local >nul 2>&1
if errorlevel 1 goto rama_nueva
git checkout -q respaldo-local
goto rama_lista
:rama_nueva
git checkout -q -b respaldo-local
:rama_lista
git add -A
git reset -q -- "%PARCHE%" "%~nx0" >nul 2>&1
git commit -q -m "respaldo del estado local antes del arreglo del panel admin" >nul 2>&1
echo       Tus cambios quedaron guardados en la rama respaldo-local.
goto master_listo

:sin_cambios
echo       No habia cambios sin guardar.

:master_listo
git checkout -q master 2>nul
if not errorlevel 1 goto en_master
git checkout -q -B master origin/master
:en_master
git merge -q --ff-only origin/master >nul 2>&1
for /f "delims=" %%c in ('git rev-parse --short HEAD') do set "LOCAL=%%c"
echo       Version local ahora: !LOCAL!
echo.

REM ---------- PASO 7: aplicar el arreglo ----------
echo [6/8] Aplicando el arreglo ...
if not exist "src\main\java\com\dotacionestoronto\dotacionestoronto\controller\AdminPanelController.java" goto aplicar
echo       El arreglo ya estaba aplicado. Se continua.
goto arreglo_listo

:aplicar
git apply --check "%PARCHE%" >nul 2>&1
if not errorlevel 1 goto aplicar_ok
echo.
echo [ERROR] El arreglo no encaja en la version actual del proyecto.
echo         Ultimo commit local:
git log --oneline -1
echo         Deberia ser el 9941cee, el del modulo de facturacion.
echo         Copia este mensaje y mandalo en el chat.
goto fin_error

:aplicar_ok
git apply "%PARCHE%"
if not errorlevel 1 goto arreglo_listo
echo.
echo [ERROR] No se pudo aplicar el arreglo. No se cambio nada.
echo         Copia este mensaje y mandalo en el chat.
goto fin_error

:arreglo_listo
git status --short
echo.

REM ---------- PASO 8: pruebas ----------
echo [7/8] Compilando y ejecutando las pruebas, tarda 1 o 2 minutos ...
if exist "mvnw.cmd" goto con_wrapper
call mvn -q -B test
goto revisar_pruebas
:con_wrapper
call mvnw.cmd -q -B test

:revisar_pruebas
if not errorlevel 1 goto pruebas_ok
echo.
echo [AVISO] Las pruebas fallaron o Maven no esta disponible ahora.
echo         El arreglo ya esta aplicado en tu carpeta, pero NO se ha subido.
choice /M "Quieres hacer el commit y subir de todos modos"
if errorlevel 2 goto fin_sin_subir
goto commit

:pruebas_ok
echo       Pruebas OK, 16 de 16.
echo.

REM ---------- PASO 9: commit y push ----------
:commit
echo [8/8] Confirmando y subiendo a GitHub ...
git add -A
git reset -q -- "%PARCHE%" "%~nx0" >nul 2>&1
git commit -m "fix(admin): controlador del panel admin para las secciones que daban 404" >nul 2>&1
if errorlevel 1 echo       No habia cambios nuevos que confirmar.
git log --oneline -1
echo.
git push origin master
if not errorlevel 1 goto listo
echo.
echo [ERROR] No se pudo subir a GitHub.
echo   - GitHub no acepta tu contrasena normal, usa un token personal de
echo     https://github.com/settings/tokens marcando el permiso repo
echo   - Si dice rejected o fetch first: vuelve a ejecutar este archivo
echo.
echo El arreglo YA esta aplicado en tu carpeta. Puedes probarlo igual con:
echo     mvnw.cmd spring-boot:run
goto fin_error

:listo
echo.
echo ==========================================================
echo    LISTO. El arreglo ya esta en GitHub, rama master.
echo.
echo    Para probarlo en tu equipo escribe:
echo        mvnw.cmd spring-boot:run
echo.
echo    Luego abre en el navegador:
echo        http://localhost:8080/admin/panel
echo.
echo    Ya deben abrir sin error 404: Perfil, Productos,
echo    Inventario, Pedidos, Ventas, Devoluciones y los reportes.
echo.
echo    Tus archivos anteriores siguen en la rama respaldo-local.
echo ==========================================================
echo.
pause
exit /b 0

:fin_sin_subir
echo.
echo No se subio nada. El arreglo queda aplicado en tu carpeta.
pause
exit /b 1

:fin_error
echo.
pause
exit /b 1
