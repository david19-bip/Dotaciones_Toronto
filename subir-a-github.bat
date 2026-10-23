@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul

REM =====================================================================
REM  Sube el modulo de Facturacion Electronica DIAN al repositorio
REM  Uso: copiar este archivo y "feat-facturacion-dian.patch" en la
REM       carpeta raiz del proyecto (donde esta pom.xml) y doble clic.
REM =====================================================================

cd /d "%~dp0"

echo.
echo ==========================================================
echo   Dotaciones Toronto - Facturacion electronica DIAN
echo ==========================================================
echo.

REM ---------- 1. Verificar que git exista ----------
git --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Git no esta instalado o no esta en el PATH.
    echo         Descargalo en: https://git-scm.com/download/win
    pause
    exit /b 1
)

REM ---------- 2. Verificar que estemos en el repositorio ----------
if not exist ".git" (
    echo [ERROR] Esta carpeta no es un repositorio Git.
    echo         Copia este archivo y el parche a la carpeta del proyecto
    echo         donde esta el archivo pom.xml.
    pause
    exit /b 1
)

if not exist "pom.xml" (
    echo [ERROR] No se encontro pom.xml. Ejecuta el script en la raiz del proyecto.
    pause
    exit /b 1
)

if not exist "feat-facturacion-dian.patch" (
    echo [ERROR] No se encontro el archivo feat-facturacion-dian.patch
    echo         Debe estar en esta misma carpeta.
    pause
    exit /b 1
)

echo Proyecto detectado: %CD%
echo.

REM ---------- 3. Identidad de Git (solo si falta) ----------
git config user.name >nul 2>&1
if errorlevel 1 (
    set /p NOMBRE=Escribe tu nombre para los commits: 
    git config user.name "!NOMBRE!"
)
git config user.email >nul 2>&1
if errorlevel 1 (
    set /p CORREO=Escribe tu correo de GitHub: 
    git config user.email "!CORREO!"
)

REM ---------- 4. Avisar de cambios sin confirmar ----------
for /f "delims=" %%i in ('git status --porcelain') do (
    echo [AVISO] Tienes cambios sin confirmar en el proyecto:
    echo         %%i
    set HAYCAMBIOS=1
)
if defined HAYCAMBIOS (
    echo.
    echo [AVISO] Se recomienda confirmar o guardar esos cambios antes de continuar.
    choice /M "Deseas continuar de todas formas"
    if errorlevel 2 exit /b 0
)

REM ---------- 5. Crear la rama y aplicar el parche ----------
echo.
echo [1/4] Creando la rama feature/facturacion-dian ...
git checkout -b feature/facturacion-dian 2>nul
if errorlevel 1 (
    echo       La rama ya existe, cambiando a ella ...
    git checkout feature/facturacion-dian
)

echo [2/4] Aplicando el parche ...
git apply --check "feat-facturacion-dian.patch" >nul 2>&1
if errorlevel 1 (
    echo       El parche ya estaba aplicado o hay conflictos.
    if exist "src\main\java\com\dotacionestoronto\dotacionestoronto\facturacion" (
        echo       El modulo ya existe en el proyecto: se continua con el commit.
    ) else (
        echo [ERROR] El parche no se puede aplicar. Revisa que el proyecto
        echo         este actualizado:  git pull origin master
        pause
        exit /b 1
    )
) else (
    git apply "feat-facturacion-dian.patch"
    if errorlevel 1 (
        echo [ERROR] Fallo al aplicar el parche.
        pause
        exit /b 1
    )
    echo       Parche aplicado correctamente.
)

REM ---------- 6. Compilar y probar ----------
echo [3/4] Compilando y ejecutando las pruebas ...
call mvnw.cmd -q -B test
if errorlevel 1 (
    echo [AVISO] Las pruebas fallaron o Maven no esta disponible aqui.
    choice /M "Deseas continuar con el commit de todas formas"
    if errorlevel 2 exit /b 1
) else (
    echo       Pruebas OK.
)

REM ---------- 7. Commit y push ----------
echo [4/4] Confirmando y subiendo los cambios ...
git add -A
git commit -m "feat(facturacion): modulo de facturacion electronica automatica con integracion DIAN"
if errorlevel 1 (
    echo       No habia cambios nuevos que confirmar.
)

git push -u origin feature/facturacion-dian
if errorlevel 1 (
    echo.
    echo [ERROR] No se pudo subir. Causas mas comunes:
    echo   1. GitHub ya no acepta contrasena: usa un token personal.
    echo      Crear en: https://github.com/settings/tokens  ^(scope repo^)
    echo      Cuando pida Password, pega el token.
    echo   2. No tienes permisos de escritura en el repositorio.
    echo.
    pause
    exit /b 1
)

echo.
echo ==========================================================
echo   LISTO. Los cambios quedaron en la rama:
echo   feature/facturacion-dian
echo.
echo   Abre el repositorio en GitHub y crea el Pull Request
echo   (aparece un boton verde "Compare ^& pull request").
echo ==========================================================
echo.
pause
