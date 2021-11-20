@echo off
java -jar ScriptConverter.jar "script.txt" "script.vxml" true
java -jar ScriptConverter.jar "script.txt" "script.min.vxml" false
java -jar ScriptConverter.jar "small_script.txt" "small_script.vxml" true
java -jar ScriptConverter.jar "small_script.txt" "small_script.min.vxml" false