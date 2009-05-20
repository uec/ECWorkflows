#!/usr/bin/perl

use strict;


# Constants
my $WINLINE = chr(0xd);



my ($psl) = @ARGV;


# First get the coords
die "Can't read $psl\n" unless (open(PSL, $psl));
my $position_by_probe = {};
while (my $line = <PSL>)
{
    chomp $line;
    my @flds = split(/\t/,$line);
    my $id = @flds[9];
    if ($id=~/^ILMN/)
    {
	my ($probe, $name) = split(/--/,$id);
	my ($strand) = @flds[8];
	my ($chr) = @flds[13];
	my ($st, $end) = @flds[15..16];
	$position_by_probe->{$probe} = join("\t",$chr, $strand, $st, $end, $name);
    }
}
close(PSL);


# Now go through illumina score file
LINE: while (my $line = <STDIN>)
{
    chomp $line;
    $line =~ s/$WINLINE//g;

    my @flds = split(/\t/,$line);

    my $probe = @flds[0];
    if ($probe=~/^Target/)
    {
	# Header
	print "$line\n";
	next LINE;
    }

    my $position_sec = $position_by_probe->{$probe};
    print "${line}\t${position_sec}\n" if ($position_sec);
}
