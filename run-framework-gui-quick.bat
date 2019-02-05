@rem Change the working directory to the location of this file so that relative paths will work
cd /D "%~dp0"

@rem Launch the GUI.
python -c "from rlbot.gui.qt_root import RLBotQTGui; RLBotQTGui.main();"

pause
