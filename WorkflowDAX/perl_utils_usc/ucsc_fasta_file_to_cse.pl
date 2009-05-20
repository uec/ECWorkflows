#!/usr/bin/perl

use strict;
use File::Basename qw/basename fileparse/;

my @files = @ARGV;

foreach my $f (@files)
{
    die "Can't open $f\n" unless (open(F,$f));

    my $fbase = fileparse($f,qr/\.[^\.]+/);
    my $outf = "${fbase}.cse";
    print STDERR "Out: $outf\n";
    die "Can't write to $outf\n" unless (open(OUTF,">$outf"));


    my $seen = {};
    my $illegal = 0;
    while (my $line = <F>)
    {
	if ($line =~ /^>/)
	{
	    die "Found illegal line:\n$line" unless ($line =~ /chr([\d\w]+)\:(\d+)\-(\d+)/i);

	    my $chr = $1;
	    my $s = $2;
	    my $e = $3;
	    $chr =~ s/X/23/ig;
	    $chr =~ s/Y/24/ig;



	    if ($chr !~ /^\d+$/)
	    {
		$illegal++;
		print STDERR "Illegal chrom: $chr\n";
	    }

	    my $out = join(",",$chr,$s,$e);
#	    print STDERR $out."\n" if ($line =~ /chrX/);
	    print OUTF $out."\n" unless ($seen->{$out});

	    $seen->{$out}++;
	}
    }

    close (OUTF);
    close(F);

    print STDERR "$fbase illegal chromosome entries seen: $illegal\n";
}

