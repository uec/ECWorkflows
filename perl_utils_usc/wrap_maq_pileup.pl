#!/usr/bin/perl

use strict;

my $USAGE = "wrap_maq_pileup.pl out.pileup \@args ref.bfa in.map";

# Argumetns
die "$USAGE\n" unless (@ARGV >= 4);
my $outfile = shift(@ARGV);
my $infile = pop(@ARGV);
my $reffile = pop(@ARGV);
my @args = @ARGV;

# Check that input exists
die "wrap_maq_pileup: input file ${infile} is 0 length\n" unless (-s $infile);

# Check that ref exists
die "wrap_maq_pileup: reference file ${reffile} is 0 length\n" unless (-s $reffile);

# Run
my $awk_part = " | awk '\$4>0' ";
my $cmd = join(" ", "maq","pileup",@args, $reffile, $infile, $awk_part,"> ${outfile}");
print STDERR "${cmd}\n";
my $output = `$cmd`;

# Check that the output exists
die "wrap_maq_pileup: output file ${outfile} is 0 length\n" unless (-s $outfile);
system("gzip $outfile");
