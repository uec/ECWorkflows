#!/usr/bin/perl

# Entries must be ordered.

use strict;
use File::Basename qw/basename fileparse/;

my @files = @ARGV;

foreach my $f (@files)
{
    die "Can't open $f\n" unless (open(F,$f));

    my $fbase = fileparse($f,qr/\.[^\.]+/);
    my $outf = "${fbase}.filled.gff";
    print STDERR "Out: $outf\n";
    die "Can't write to $outf\n" unless (open(OUTF,">$outf"));


    my $last_chr = "0";
    my $last_end = -1;
    while (my $line = <F>)
    {
	chomp $line;
	my @flds = split(/\t/,$line);
	print STDERR $line unless (@flds >= 5);

	my $c = @flds[0];
	my $s = @flds[3];
	my $e = @flds[4];

	if ($last_chr eq $c)
	{
	    if ($s < $last_end)
	    {
		print STDERR "\t\t$c : start $s less than last end $last_end\n";
	    }
	    elsif (($s - $last_end) > 5000)
	    {
		print STDERR "Gap of more than 5kb ($c:${last_end}-${s}), skipping\n";
	    }
	    else
	    {
		@flds[3] = $last_end + 1;
	    }
	}
	$last_chr = $c;
	$last_end = $e;

	my $out = join("\t",@flds);
	print OUTF $out."\n";
    }

    close (OUTF);
    close(F);
}
