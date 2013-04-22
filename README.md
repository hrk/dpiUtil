# dpiUtil

## What is this?

A tool to quickly get a visual comparison of Android resources
in a project.
It was built as an helper to generate LDPI resources for a Samsung
Galaxy GT-S5570 (Mini/Tass) for Jelly Bean, as Google has deprecated
LDPI devices, and the official repository still has Gingerbread icons
which look out of place in a Holo enviroment.

## Arguments

They're documented if you run the tool with no arguments.

java -jar dpiUtil.jar

## Example of usage

java -jar dpiUtil.jar --root android/system/frameworks/base/packages/SystemUI/res --overlay ~/android/android_device_ldpi-common/overlay/frameworks/base/packages/SystemUI/res/drawable-ldpi/ > frameworks_base_packages_SystemUI_overlay.html 

## Required libraries

Java Minimal Template Engine: http://http://jmte.googlecode.com/
Argparse4J: http://argparse4j.sourceforge.net/
