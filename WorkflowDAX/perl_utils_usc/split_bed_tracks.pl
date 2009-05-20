#!/usr/bin/perl

use strict;

my ($prefix, @fns)  = @ARGV;


foreach my $fn (@fns)
{
    die "Couldn't open $fn" unless (open(F,$fn));

    my $sec = '';
    my $sec_name = '';
    while (my $line = <F>)
    {
	if ($line =~ /track\s+name=(\S+)/)
	{
	    print_sec($sec, $sec_name, $prefix);
	    $sec_name = $1;
	    $sec = '';
	}
	$sec .= $line;
    }
    print_sec($sec, $sec_name, $prefix); # Last one

    close(F);
}


sub print_sec
{
    my ($sec, $sec_name,$prefix) = @_;

    if ($sec)
    {
	my $l = length($sec);
	my $fn = "${sec_name}";
	$fn .= "_${prefix}" if ($prefix);
	$fn .= ".bed";

	print "$l\t$fn\n";
	
	die "Couldn't write to $fn\n"  unless (open(OUTF,">$fn"));
	print OUTF $sec;
	close(OUTF);
    }
}
