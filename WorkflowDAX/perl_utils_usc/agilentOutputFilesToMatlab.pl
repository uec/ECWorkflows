#!/usr/bin/perl
# See file column mappings at end of file.

use strict;
use File::Basename qw/basename/;

# GLOBALS
my $USAGE = "agilentOutputFilesToMatlab.pl file1.txt file2.txt ... ";

my $PREFIX = "USC-AgilentProcessed-";

my @NUMERIC_COLS = ();
#push(@NUMERIC_COLS, qw/LogRatio LogRatioError PValueLogRatio/);
#push(@NUMERIC_COLS, qw/gProcessedSignal rProcessedSignal gProcessedSigError rProcessedSigError/);
#push(@NUMERIC_COLS, qw/gIsPosAndSignif rIsPosAndSignif gIsWellAboveBG rIsWellAboveBG/);
push(@NUMERIC_COLS, qw/gBGSubSignal rBGSubSignal gBGSubSigError rBGSubSigError/);

my @TEXT_COLS = ();
#push(@TEXT_COLS, qw/accessions GeneName ProbeName SystematicName Description/);


my $exp_fn = "${PREFIX}exps.txt";
die "Can't write to $exp_fn\n" unless (open(EXP,">$exp_fn"));

# GO THROUGH FILES
my $col_fhs = {};
for (my $in_i = 0; $in_i < scalar(@ARGV); $in_i++)
{
    my $infn = @ARGV[$in_i];

    my $inbase = File::Basename::basename($infn, ('.txt'));
    print EXP "$inbase\n";
    print STDERR "Processing $infn ($inbase)\n";

    # Read file
    die "Couldn't read $infn\n" unless (open(INF, $infn));
    my $in_feats_sec = 0;
    my $colmap = {};
    my $on_feat = 0;
    LINE: while (my $line = <INF>)
    {
	chomp $line;
	my @cols = split(/\t/,$line);
	my $coltype = @cols[0];

#	print STDERR "${inbase}\t$coltype\n";

	if ($coltype eq 'FEATURES')
	{
	    $in_feats_sec = 1;
#	    print STDERR "\tEntering feature sec\n";
	    # Make the colmap
	    for (my $i=0; $i<scalar(@cols); $i++)
	    {
		my $col = $cols[$i];
		$colmap->{$col} = $i;
	    }
	}
	elsif ($coltype eq 'DATA')
	{
	    if ($in_feats_sec)
	    {
		# Don't want controls
		my $control_col = $colmap->{ControlType};
		next LINE unless ($cols[$control_col] == 0);

		$on_feat++;
		print STDERR "On feat $on_feat\n" unless ($on_feat % 1000);

		my @to_do = ($in_i == 0) ? (0,1) : (1); # Only do the text on the first round
		foreach my $numeric (@to_do)
		{
		    my $typename = ($numeric) ? "NUMERIC" : "TEXT";
		    foreach my $c (($numeric) ? @NUMERIC_COLS : @TEXT_COLS)
		    {
			my $col = $colmap->{$c};
			die "Can't find col $c in $inbase\n" unless (defined($colmap->{$c}));

			# Make sure the file is started
			my $col_fh = $col_fhs->{$c};
			if (!$col_fh)
			{
			    my $colfn = "${PREFIX}${typename}-${c}.".(($numeric)?"csv":"txt");
#			    print STDERR "\t\t\tSTARTING FILE $colfn\n";
			    die "Couldn't write to file $colfn\n" unless (open($col_fh, ">$colfn"));
			    $col_fhs->{$c} = $col_fh;
			}
			else
			{
			    # Print a newline
			    print $col_fh "\n" if ($on_feat==1);
			}
			# And add data to the file
			my $val = $cols[$col];
			print $col_fh (($numeric)?",":"\n") unless ($on_feat == 1);
			print $col_fh (($numeric)? sprintf("%0.2e",$val) : "$val");
#			print STDERR "\tONFEAT=$on_feat\t$typename\t$c\t$val\n";
		    }
		}
	    }
	}
	else
	{
	    # Don't worry about other columns
	}
    }


    close(INF);
}



# Close files
foreach my $colfh (values(%$col_fhs))
{
    print $colfh "\n"; # Final newline
    close($colfh);
}

close(EXP);















# 1	FEATURES
# 2	FeatureNum
# 3	Row
# 4	Col
# 5	accessions
# 6	SubTypeMask
# 7	SubTypeName
# 8	Start
# 9	Sequence
# 10	ProbeUID
# 11	ControlType
# 12	ProbeName
# 13	GeneName
# 14	SystematicName
# 15	Description
# 16	PositionX
# 17	PositionY
# 18	LogRatio
# 19	LogRatioError
# 20	PValueLogRatio
# 21	gSurrogateUsed
# 22	rSurrogateUsed
# 23	gIsFound
# 24	rIsFound
# 25	gProcessedSignal
# 26	rProcessedSignal
# 27	gProcessedSigError
# 28	rProcessedSigError
# 29	gNumPixOLHi
# 30	rNumPixOLHi
# 31	gNumPixOLLo
# 32	rNumPixOLLo
# 33	gNumPix
# 34	rNumPix
# 35	gMeanSignal
# 36	rMeanSignal
# 37	gMedianSignal
# 38	rMedianSignal
# 39	gPixSDev
# 40	rPixSDev
# 41	gPixNormIQR
# 42	rPixNormIQR
# 43	gBGNumPix
# 44	rBGNumPix
# 45	gBGMeanSignal
# 46	rBGMeanSignal
# 47	gBGMedianSignal
# 48	rBGMedianSignal
# 49	gBGPixSDev
# 50	rBGPixSDev
# 51	gBGPixNormIQR
# 52	rBGPixNormIQR
# 53	gNumSatPix
# 54	rNumSatPix
# 55	gIsSaturated
# 56	rIsSaturated
# 57	PixCorrelation
# 58	BGPixCorrelation
# 59	gIsFeatNonUnifOL
# 60	rIsFeatNonUnifOL
# 61	gIsBGNonUnifOL
# 62	rIsBGNonUnifOL
# 63	gIsFeatPopnOL
# 64	rIsFeatPopnOL
# 65	gIsBGPopnOL
# 66	rIsBGPopnOL
# 67	IsManualFlag
# 68	gBGSubSignal
# 69	rBGSubSignal
# 70	gBGSubSigError
# 71	rBGSubSigError
# 72	BGSubSigCorrelation
# 73	gIsPosAndSignif
# 74	rIsPosAndSignif
# 75	gPValFeatEqBG
# 76	rPValFeatEqBG
# 77	gNumBGUsed
# 78	rNumBGUsed
# 79	gIsWellAboveBG
# 80	rIsWellAboveBG
# 81	gBGUsed
# 82	rBGUsed
# 83	gBGSDUsed
# 84	rBGSDUsed
# 85	IsNormalization
# 86	gDyeNormSignal
# 87	rDyeNormSignal
# 88	gDyeNormError
# 89	rDyeNormError
# 90	DyeNormCorrelation
# 91	ErrorModel
# 92	xDev
# 93	gSpatialDetrendIsInFilteredSet
# 94	rSpatialDetrendIsInFilteredSet
# 95	gSpatialDetrendSurfaceValue
# 96	rSpatialDetrendSurfaceValue
# 97	SpotExtentX
# 98	SpotExtentY
# 99	gNetSignal
# 100	rNetSignal
# 101	gMultDetrendSignal
# 102	rMultDetrendSignal
# 103	gProcessedBackground
# 104	rProcessedBackground
# 105	gProcessedBkngError
# 106	rProcessedBkngError
# 107	IsUsedBGAdjust
# 108	gInterpolatedNegCtrlSub
# 109	rInterpolatedNegCtrlSub
# 110	gIsInNegCtrlRange
# 111	rIsInNegCtrlRange
# 112	gIsUsedInMD
# 113	rIsUsedInMD
