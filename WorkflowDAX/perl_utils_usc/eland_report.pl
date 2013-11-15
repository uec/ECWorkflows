#!/usr/bin/perl

use strict;

use File::Basename qw/basename/;

my $USAGE = "eland_report.pl eland1.txt eland2.txt";

print STDERR "$USAGE\n" unless (@ARGV>0);

my $outfn = "counts.txt";
die "Can't open $outfn\n" unless (open(OUTFILE,">$outfn"));

foreach my $fn (@ARGV)
{
    my $base = basename($fn, (qw/.txt/));

    die "Can't read $fn\n" unless (open(INFILE,$fn));
    my $counts = {};
    while (my $line = <INFILE>)
    {
	chomp $line;
	my ($id, $seq, $code, $n_perfect, $n_one, $n_two, $chr, $pos, $strand, $mm1, $mm2) = split(/\t/,$line);

	my $genome = "nochr";
	if ($chr =~ /chr/i)
	{ 
	    $genome = "human";
	}
	elsif ($chr =~ /contam/)
	{
	    $genome = $chr;
	}

	$code = "${code}.$genome";

	$counts->{$code}++;
	$counts->{substr($code,0,1)}++; # First character is the class
    }
    close(INFILE);

    print OUTFILE "$fn\n";
    for my $code (sort keys(%$counts))
    {
	my $count = $counts->{$code};
	print OUTFILE "\t${code}: $count\n";
    }
    print OUTFILE "\n";
}

close(OUTFILE);
