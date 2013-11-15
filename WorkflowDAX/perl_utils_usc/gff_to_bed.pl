#!/usr/bin/perl

use strict;

while (my $line = <>)
{
    chomp $line;
    if (($line =~ /^\s*track/i) || ($line =~ /^\s*browser/i))
    {
	print $line."\n";
	next;
    }

    my @flds = split(/\t/,$line);

    my $name = "";

    if (@flds[6] eq '-')
    {
	my $tmp = @flds[3];
	@flds[3] = @flds[4];
	@flds[4] = $tmp;
    }


    if (@flds[8] =~ /gene_id\s+\"([^\"]*)\"/)
    {
	$name = $1;
    }

    print join("\t",@flds[0], @flds[3..4], $name, @flds[5])."\n";
}
