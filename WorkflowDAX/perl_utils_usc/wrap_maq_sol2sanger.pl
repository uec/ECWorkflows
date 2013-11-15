#!/usr/bin/perl

use strict;

my $USAGE = "wrap_maq_sol2sanger.pl a.sfq a.fq";

# Argumetns
die "$USAGE\n" unless (@ARGV == 2);
my ($infile, $outfile) = @ARGV;

# Check that input exists
die "wrap_maq_sol2sanger: input file ${infile} is 0 length\n" unless (-s $infile);

# Run
my $cmd = join(" ", "maq","sol2sanger",@ARGV);
print STDERR "${cmd}\n";
print STDERR `${cmd} 2>&1`;

# Check that the output exists
die "wrap_maq_sol2sanger: output file ${outfile} is 0 length\n" unless (-s $outfile);

