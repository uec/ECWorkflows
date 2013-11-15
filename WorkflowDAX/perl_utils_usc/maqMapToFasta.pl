#!/usr/bin/perl

use strict;

my $USAGE = "fastqToFasta.pl [start end] > out (start end are 1-based, inclusive)";
my ($start,$end) = @ARGV;
my $len = $end-$start+1;

while (my $line = <STDIN>)
{
    chomp $line;
    my @flds = split(/\t/,$line);
    my $name = $flds[0];
    my $seq = $flds[14];
    
    if ($start)
    {
	$seq = substr($seq,$start-1,$len);
    }

    print ">${name}\n${seq}\n";
}
