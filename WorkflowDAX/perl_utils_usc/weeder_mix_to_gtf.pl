#!/usr/bin/perl

use strict;
use File::Basename qw/basename/;
use File::Temp qw/:POSIX :mktemp tempfile/;


# CONSTANTS
my $FA_BASE = "weederFaFileXXXXX";

# GLOBALS
$::ON_SITE_ID = 1;
$::TEMPFILES = {}; # Cleanup everything in this hash


# ARGS 
die "USAGE: weeder_mix_to_gtf.pl file.fa file.mix score_min(0-100)\n" unless (@ARGV == 3);
my ($in_fa, $in_mix, $score_min) = @ARGV;


# Copy our file to a tempfile
my ($fh, $fa) = tempfile( $FA_BASE, SUFFIX => '.fa', DIR => '/tmp'); 
close($fh);
`cp $in_fa $fa`;
$::TEMPFILES->{$fa}++;
print STDERR "fa=$fa\n";

# PArse mix file
die "Couldn't open $in_mix\n" unless (open(MIX, $in_mix));
while (my $line = <MIX>)
{
    chomp $line;
    if ($line =~ /^\s*(\d+)\)\s+(\w+)\s+(\S+)\s+(\d+)\s*$/)
    {
	my ($motif_n, $res, $score, $mm) = ($1, $2, $3, $4);
	my $l = length($res);
	my $motif_n = "l${l}m${motif_n}_sc".int($score*100)."_${res}_mm${mm}";
	print STDERR "Working on motif $motif_n\n";

	my $gtf_str = printSites($motif_n, $res, $mm, $fa, $score_min);
    }
    else
    {
	die "Illegal line: $line\n";
    }
}
close(MIX);

# cleanup
foreach my $fn (keys(%$::TEMPFILES))
{
    unlink($fn);
}


sub printSites
{
    my ($motif_n, $res, $mm, $fa, $thresh) = @_;

    my $l = length($res);

    # Run locator
    my $cmd = "locator.out ${fa} $res $mm $thresh S";
    print STDERR "\tRunning ${cmd} ...\n";
    `$cmd >& /dev/null`;

    # Get the wee filename
    die "Incorrect FORmat? $fa\n" unless ($fa =~ /^(.*)\.fa[^\.]*$/);
    my $fa_base = $1;
    my $wee = "${fa_base}.wee";
    $::TEMPFILES->{$wee}++;
    my $html = "${fa_base}.html";
    $::TEMPFILES->{$html}++;
    
    my $wee = $fa;
    $wee =~ s/\.fa[^\.]*/\.fa.${res}.wee/g;

    die "\tCouldn't open wee file $wee\n" unless (open(WEE, $wee));
    my ($src_chr, $src_s, $src_e) = ();
    my $cur_strand = ""; # This always comes on the line before
    my $num_sites = 0;
    while (my $line = <WEE>)
    {
	chomp $line;

	if ($line =~ /^\s*>/)
	{
	    die "Bad wee line ($wee): $line\n"
		unless ($line =~ /(chr\d+)\:(\d+)\-(\d+)/);
	    ($src_chr, $src_s, $src_e) = ($1, $2, $3);
	}
	elsif ($line =~ /^\s*([\+\-])\s+/)
	{
	    $cur_strand = $1;
	}
	elsif ($line =~ /position\s+(\d+),\s+\(([^\)]+)\)/)
	{
	    die "Why are we hitting a wee position before having \$src_chr set?\nLINE: $line" unless ($src_chr);
	    my ($rel_s, $score) = ($1, $2);
	    if ($score >= $thresh)
	    {
		my $abs_s = $src_s + $rel_s - 1;
		my $abs_e = $abs_s + $l - 1;

		my $id = "${motif_n}_".$::ON_SITE_ID++;
		my $atts = "gene_id \"$id\"; transcript_id \"$id\";";
		print join("\t", $src_chr, $motif_n, "exon", $abs_s, $abs_e, $score, $cur_strand, ".", $atts);
		print "\n";
		$num_sites++;
	    }
	}
    }

    close(WEE);
    print STDERR "\tFound ${num_sites} sites ($motif_n)\n";
}


