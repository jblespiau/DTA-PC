DTA-Simulator
=============

General simulator for the Dynamic Traffic Assignment with multiple commodities,
multiple origins and destinations with partial compliance.
All the code is in java and all dependencies are included.

Don't forget to enable assertions (-ea parameter on the JVM).


Possible improvements and optimizations
---------------------------------------

1) Register only the densities and flows for a given cell for all commodities
that goes through that cell. To do so
- Precompute when adding the path the possible commodities through the cells
- Register only the values for these commodities in the block describing the
cell in the x vector
2) Parallelize the forward computation and possibly the solving of the adjoint
equations

Linking to IpOpt
============
The gradient descent method has been coded in Java in order to have full control
on what is happening during the optimization.

One may try t use IpOpt instead. Then, having IpOpt installed is mandatory.
To build it see http://www.coin-or.org/download/source/Ipopt/ and take the 
most recent version (the version 3.10.3 was used in this project). 
Then follow the instructions in /Ipopt-3.xx.x/Ipopt/doc/documentation.pdf

Moreover, you need lib/libjipopt.so which is the java wrapper for Ipopt.
Some binaries are available for some platforms at:
https://projects.coin-or.org/Ipopt/wiki/JavaInterface

If there is not the binaries for your platform, you will have to compile it
from the sources. To do so, look at:
/Ipopt-3.xx.x/Ipopt/contrib/JavaInterface/README

