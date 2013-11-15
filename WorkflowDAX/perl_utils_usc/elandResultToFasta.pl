#!/usr/bin/perl

use strict;

while (my $line = <>)
{
    chomp $line;
    my @flds = split(/\t/,$line);

    if ($flds[2] =~ /^U/)
    {

	my $name = $flds[0];
	my $seq = $flds[1];
    print ">${name}\n${seq}\n";
    }
    

}
