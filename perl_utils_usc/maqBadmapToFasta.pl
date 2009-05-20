#!/usr/bin/perl

use strict;

while (my $line = <>)
{
    chomp $line;
    my ($name,$score,$seq,$qual) = split(/\t/,$line);

    print ">${name}\n${seq}\n";
}
