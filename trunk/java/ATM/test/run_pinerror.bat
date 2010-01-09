set TEST_DIR=testsuite_pinerror
for %%f in (%TEST_DIR%/*) do call :test %TEST_DIR%/%%f || goto :EOF
goto :EOF

:test
java -Djava.util.logging.config.file=logging.properties -jar ../dist/ATM.jar -f %1 10000
