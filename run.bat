@echo off
echo ============================================
echo   Lancement de Bio_Backend
echo   (nettoyage + recompilation complete + demarrage)
echo ============================================
call mvnw clean spring-boot:run
echo.
echo Le serveur s'est arrete. Appuie sur une touche pour fermer cette fenetre.
pause
