#!/usr/bin/perl

use strict;

my $USAGE = "qdelRange.pl start end";

die "$USAGE\n" if (@ARGV != 2);

my ($s, $e) = @ARGV;

for (my $i = $s; $i <= $e; $i++)
{
    my $cmd = "qdel $i";
    print STDERR "${cmd}\n";
    `$cmd`;
}
