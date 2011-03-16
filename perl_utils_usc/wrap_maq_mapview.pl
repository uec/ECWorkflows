#!/usr/bin/perl

use strict;

my $USAGE = "mapview out.map.txt in.map 30";
die "$USAGE\n" unless (@ARGV == 3);

# Check that input exists
my $outfile = $ARGV[0];
my $infile = $ARGV[1];
my $q = $ARGV[2];
die "wrap_mapview: input file ${infile} is 0 length\n" unless (-s $infile);

my $awk_part = " | awk '\$7>=$q' ";
my $cmd = join(" ", "maq", "mapview", $infile, $awk_part,"> ${outfile}");
print STDERR "$cmd\n";
print STDERR `$cmd`;

# Check that the output exists
die "wrap_mapview: output file ${outfile} is 0 length\n" unless (-s $outfile);

