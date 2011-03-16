#!/usr/bin/perl

use strict;

while (my $line = <>)
{
    chomp $line;
    next if ($line =~ /track/i);

    my @flds = split(/\t/,$line);

    my $name = "";

    if (@flds[8] =~ /gene_id\s+\"?(\w+)\"?/)
    {
	$name = $1;
    }

    print join(",",@flds[0], @flds[3..4], $name)."\n";
}
