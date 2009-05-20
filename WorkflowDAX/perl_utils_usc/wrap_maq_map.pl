#!/usr/bin/perl

use strict;

my $USAGE = "wrap_maq_map.pl \@args out.map ref.bfa in.bfq";

# Argumetns
die "$USAGE\n" unless (@ARGV >= 3);
my $infile = pop(@ARGV);
my $reffile = pop(@ARGV);
my $outfile = pop(@ARGV);

# Check that input exists
die "wrap_maq_map: input file ${infile} is 0 length\n" unless (-s $infile);

# Check that ref exists
die "wrap_maq_map: reference file ${reffile} is 0 length\n" unless (-s $reffile);

# Run
my $cmd = join(" ", "maq","map",@ARGV, $outfile, $reffile, $infile);
print STDERR "${cmd}\n";
#print STDERR `${cmd} 2>&1`;
system("${cmd} 2>&1");

# Check that the output exists
die "wrap_maq_map: output file ${outfile} is 0 length\n" unless (-s $outfile);

