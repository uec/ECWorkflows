#!/usr/bin/perl

use strict;

my $USAGE = "fastqToFasta.pl [start end] > out (start end are 1-based, inclusive)";

my ($start,$end) = @ARGV;

my $len = $end-$start+1;

while (my $line = <STDIN>)
{

    chomp $line;
    if (substr($line,0,1) eq '@')
    {
	print ">" . substr($line,1) . "\n";
    }
    elsif ($line =~ /^[ACTGN]+$/i)
    {
	my $seq;
	if ($start)
	{
	    $seq = substr($line,$start-1,$len);
	}
	else
	{
	    $seq = $line;
	}
	
	print $seq . "\n";
    }
}
