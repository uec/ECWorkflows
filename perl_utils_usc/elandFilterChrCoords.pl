#!/usr/bin/perl

use strict;

use File::Basename qw/basename/;

my $USAGE = "elandFilterChrCoords chr21 1000 2000 unique_only eland_file1.txt eland_file2.txt";

print STDERR "$USAGE\n" unless (@ARGV>=4);

my $target_chr = lc(shift @ARGV);
my $s = shift(@ARGV);
my $e = shift(@ARGV);


foreach my $fn (@ARGV)
{
    my $base = basename($fn, (qw/.txt/));

    die "Can't read $fn\n" unless (open(INFILE,$fn));
    my $counts = {};
    while (my $line = <INFILE>)
    {
	chomp $line;
	my ($id, $seq, $code, $n_perfect, $n_one, $n_two, $chr, $pos, $strand, $mm1, $mm2) = split(/\t/,$line);

	    if (!$unique_only || (substr($code,0,1) eq 'U'))
	    {
		print OUTFILE $line."\n";
	    }


	if (lc($chr) eq $target_chr)
	{
	    if (($pos >= $s) && ($pos <= $e))
	    {
		print $line."\n";
	    }
	}
    }
    close(INFILE);
}
