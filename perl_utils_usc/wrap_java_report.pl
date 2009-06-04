#!/usr/bin/perl
use strict;
my $outputFile = shift @ARGV;
system(join(" ", @ARGV) . " >$outputFile");
