DTA-Simulator
=============

General simulator for the Dynamic Traffic Assignment with multiple commodities,
multiple origins and destinations with partial compliance

Don't forget to enable assertions (-ea parameter on the JVM).

Requirements
============
You need to have installed ipopt on your computer (and eventually build it from
sources if there is no binary available). 
To build it see http://www.coin-or.org/download/source/Ipopt/ and take the 
most recent version (the version 3.10.3 was used in this project). 
Then follow the instructions in /Ipopt-3.xx.x/Ipopt/doc/documentation.pdf

Moreover, you need lib/libjipopt.so which is the java wrapper for Ipopt.
Some binaries are available for some platforms at:
https://projects.coin-or.org/Ipopt/wiki/JavaInterface

If there is not the binaries for your platform, you will have to compile it
from the sources. To do so, look at:
/Ipopt-3.xx.x/Ipopt/contrib/JavaInterface/README
