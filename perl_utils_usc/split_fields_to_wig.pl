#!/usr/bin/perl

use strict;
use File::Basename;
use List::Util qw(min max);

$::USAGE = "split_fields_to_wig.pl TITLE file1.txt file2.txt ...";

$::HUMAN = 1;
$::DELIM = "\\s+";

$::FLD_MAP = {
    'pos_fld' => 0,
    'score_fld' => 1,
    'chr_field' => 10 };

my ($pre, @in_files) = @ARGV;

die "$::USAGE\n" if (-f $pre);

my $tracks = {};
foreach my $f (@in_files)
{
    my $track_n = $f;
    $track_n =~ s/_?chr\d+//g;
    
    my $chr = 0;
    my $track_out = "";
    
    # Read file
    die "Can't read $f\n" unless (open(FILE,$f));
    while (my $line = <FILE>)
    {
	next if (($line =~ /^\s*\#/) || ($line =~ /^\s*$/));

	chomp $line;
	my @flds = split($::DELIM, $line);

	# Get the chromosome , unless we already have it.
	if (!$chr)
	{
	    $chr = $flds[$::FLD_MAP->{chr_field}];
	    if (!$chr && ($f =~ /chr(\d+)/i))
	    {
		$chr = "chr".$1;
	    }
	}

	# And dump the line
	$track_out .= (25+($flds[$::FLD_MAP->{pos_fld}]))."\t".$flds[$::FLD_MAP->{score_fld}]."\n";
	# $track_out .= $flds[$::FLD_MAP->{pos_fld}]."\t".$flds[$::FLD_MAP->{score_fld}]."\n";
    }
    close(FILE);

    $track_out = "variableStep\t chrom=$chr\n" . $track_out;
    $tracks->{$track_n} .= $track_out;
}

# Now print the tracks
foreach my $track (sort(keys(%$tracks)))
{
    # Print the track header
    print "track type=wiggle_0 name=\"${pre}-${track}\" description=\"${pre}-${track}\" visibility=2 windowingFunction=maximum ";
    print "viewLimits=0:2 maxHeightPixels=128:20:11 windowingFunction=maximum autoScale=off";
    print "\n";

    # And the track
    print $tracks->{$track};
}





