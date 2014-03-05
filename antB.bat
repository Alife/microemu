@if not defined Java_Home call ../.path.bat
@set microemulator_dir=microemulator.git\microemulator
@rem set jarfile=%1 
@set jarfile=Opera Mini 4.22
@set jarDir=D:\Java\workspace\microemu_mod
@set paras=  
@rem set paras=%paras% -Dmidlet.jad="%jarDir%\%jarfile%.jad" 
@rem set paras=%paras% -Dmidlet.jar="%jarDir%\%jarfile%.jar" 
@set paras=%paras% -Dmidlet.jar=%1 
@rem set paras=%paras% -Dmidlet.package="%jarfile%.apk"
@rem set paras=%paras% -Dout-packagee="%jarfile%.apk"
@set paras=%paras% -Dsdk-folder=%sdk-folder%
@set paras=%paras% -Dandroid-jar=%sdk-platform-folder%/android.jar
@set paras=%paras% -Dsdk-platform-folder=%sdk-platform-folder%
@set paras=%paras% -Daapt=%sdk-folder%\platform-tools\aapt
@set paras=%paras% -Ddx=%sdk-folder%\platform-tools\dx.bat
@set paras=%paras% -Dmicroemu-cldc.jar=libs\microemu-cldc-3.0.0-SNAPSHOT.jar
@set paras=%paras% -Dmicroemu-midp.jar=libs\microemu-midp-3.0.0-SNAPSHOT.jar
@set paras=%paras% -Dmicroemu-javase.jar=libs\microemu-javase-3.0.0-SNAPSHOT.jar
@set paras=%paras% -Dmicroemu-jsr-75.jar=libs\microemu-jsr-75.jar
@rem set paras=%paras% -Dasm.jar=%cd%/asm-3.2.jar
@rem set paras=%paras% -Doutdir=%cd%\%microemulator_dir%\microemu-android\bin
@rem echo %paras%
@rem ant -buildfile D:\Java\workspace\microemulator.git\microemulator\microemu-android\build.xml %1 %paras%
@ant -buildfile build.xml %2 %paras%
@rem ant -buildfile build.apk.xml %1 %paras%

rem        <basename property="prepared.midlet.jar.base" file="${midlet.jar}"/>
rem        <dirname property="prepared.midlet.jar.dir" file="${midlet.jar}"/>

:: adb install -r "D:\Java\workspace\microemulator.git\microemulator\microemu-android\bin\Opera Mini 4.22.apk"
:: adb install -r "%cd%\%microemulator_dir%\microemu-android\bin/%jarfile%.apk"
:: ant -buildfile %microemulator_dir%\microemu-android\build.xml reinstall
