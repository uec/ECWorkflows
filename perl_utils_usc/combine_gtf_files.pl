#!/usr/bin/perl

use strict;
use File::Basename qw/basename/;

# File names are used as track names
$::USAGE = "combine_gtf_files [VISIBILITY] file1.gtf file2.gtf ...";


@::COLORS = (
	     "70,130,180",
	     "250,128,114",
#	     "124,252,0",
	     "0,252,0",
	     "139,0,139",
	     "0,153,153",
	     "204,204,0",
	     "102,255,255",
	     "204,152,102",
	     "204,0,255",
	     "102,255,102");


my $vis = 0;
if (@ARGV[0] =~ /^\d+$/)
{
    $vis = @ARGV[0];
    shift @ARGV;
}

my @files = @ARGV;
die $::USAGE."\n" unless (@files);

my $color_idx = 0;
foreach my $file (@files)
{
    die "Can't open $file\n" unless (open(F,$file));
    my $base = basename($file, (".gff",".gtf"));

    my $line_num = 1;
    LINE: while (my $line = <F>)
    {
	chomp $line;

	# Add a track name if there isn't one 
	if ($line_num++ == 1)
	{
	    my $has_track = ($line =~ /track/i);

	    my $trackline = ($has_track) ? $line : "track name='${base}'";

	    if ($trackline !~ /name/i)
	    {
		$trackline .= " name='${base}'";
	    }
	    
	    if ($trackline !~ /color/i)
	    {
		$color_idx = 0 if ($color_idx >= @::COLORS);
		my $next_color = @::COLORS[$color_idx];
		$color_idx++;
		$trackline .= " color='$next_color'";
	    }

	    if ($vis)
	    {
		$trackline .= " visibility=\"$vis\"";
	    }

	    print "${trackline}\n";
	    print "${line}\n" unless ($has_track);
	}
	else
	{
	    my $n = $base . "-$line_num";
	    $line =~ s/gene_id \"[^\"]+\"; transcript_id \"[^\"]+\"/gene_id \"$n\"; transcript_id \"$n\"/gi;
	    print "${line}\n";
	}
    }

    close(F);
}
