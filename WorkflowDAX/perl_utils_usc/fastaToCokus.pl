#!/usr/bin/perl

use strict;

while (my $line = <STDIN>)
{
    if ( ($line =~ /^\s*\#/) || ($line =~ /^\s*>/) )
    {
    }
    else
    {
	chomp $line;
	print uc($line);
    }
}
print "\n";
