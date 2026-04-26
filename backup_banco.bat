@echo off
setlocal

:: =========================================
:: Script de Backup Automático do PostgreSQL
:: =========================================

:: Configurações do Banco de Dados
set PGHOST=localhost
set PGPORT=5432
set PGUSER=postgres
set PGPASSWORD=micro123
set PGDATABASE=saude_ocupacional

:: Diretório de Backup
set BACKUP_DIR=C:\Backups\SaudeOcupacional
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

:: Nome do arquivo de backup com data e hora
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set datetime=%%I
set BACKUP_FILE=%BACKUP_DIR%\backup_saude_%datetime:~0,4%%datetime:~4,2%%datetime:~6,2%_%datetime:~8,2%%datetime:~10,2%%datetime:~12,2%.sql

:: Executa o pg_dump
echo Realizando backup do banco de dados %PGDATABASE%...
"C:\Program Files\PostgreSQL\16\bin\pg_dump.exe" -h %PGHOST% -p %PGPORT% -U %PGUSER% -F c -b -v -f "%BACKUP_FILE%" %PGDATABASE%

if %ERRORLEVEL% equ 0 (
    echo Backup concluido com sucesso: %BACKUP_FILE%
) else (
    echo Erro ao realizar o backup!
)

:: Mantém apenas os backups dos últimos 7 dias
echo Removendo backups mais antigos que 7 dias...
forfiles /p "%BACKUP_DIR%" /s /m *.sql /d -7 /c "cmd /c del @path"

endlocal
