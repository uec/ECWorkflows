#!/usr/bin/perl

use strict;

my @files = @ARGV;

foreach my $f (@files)
{
    die unless (-s $f);
}
