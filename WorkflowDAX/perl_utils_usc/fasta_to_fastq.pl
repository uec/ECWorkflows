#!/usr/bin/perl

use strict;

while (my $line = <STDIN>)
{
    chomp $line;
    if (substr($line,0,1) eq '@')
    {
	print ">" . substr($line,1) . "\n";
    }
    elsif ($line =~ /^[ACTGN][ACTGN][ACTGN]/i)
    {
	print $line . "\n";
    }
}
