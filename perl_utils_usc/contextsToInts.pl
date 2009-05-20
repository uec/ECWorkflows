#!/usr/bin/perl

use strict;

while (<>)
{
    s/A/1/gi; s/C/2/gi; s/T/3/gi; s/G/4/gi;
    print;
}
