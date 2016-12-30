#!/usr/bin
tdir=~/sysdev/bin

lein bin
echo "Moving to:" $tdir
cp -f target/mdu $tdir
echo "All done, have a nice day!"
