#!/usr/bin/perl

use strict;

my ($col, $delim) = @ARGV;
$delim = "\t" unless ($delim);


while (my $line = <STDIN>)
{
    chomp $line;
    my @a = split(/$delim/,$line);
    print @a[$col-1]."\n";
}
