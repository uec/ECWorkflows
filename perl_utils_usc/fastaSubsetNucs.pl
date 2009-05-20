#!/usr/bin/perl

use strict;

my $USAGE = "fastaToSubsetNucs.pl start-pos end-pos < in.fa > out.fa";

die "$USAGE\n" unless (@ARGV==2);

my ($s,$e) = @ARGV;

while (my $line = <STDIN>)
{
    chomp $line;
    if ($line !~ /^>/)
    {
	$line = substr($line,$s-1, $e-$s+1);
    }

    print $line."\n";
}
