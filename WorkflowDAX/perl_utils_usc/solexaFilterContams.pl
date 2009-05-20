#!/usr/bin/perl
use strict;

my @files      = @ARGV;
my $MAX_A_FRAC = 0.9;

foreach my $f (@files) {

	# Open input
	open( my $FHin, $f ) || die "Can't read $f\n";

	#OUTPUTS
	##polya contams
	my $outpolya = $f;
	$outpolya =~ s/\.(\w+)$/.contam.polya\.$1/g;
	die "Can't write to $outpolya\n" unless ( open( OUTPOLYA, ">$outpolya" ) );

	##adaptor contams
	my $outadapters = $f;
	$outadapters =~ s/\.(\w+)$/.contam.adapters\.$1/g;
	die "Can't write to $outadapters\n"
	  unless ( open( OUTADAPTERS, ">$outadapters" ) );

	##adaptor trim contams
	my $outadaptertrim = $f;
	$outadaptertrim =~ s/\.(\w+)$/.contam.adapterTrim\.$1/g;
	die "Can't write to $outadaptertrim\n"
	  unless ( open( OUTADAPTERTRIM, ">$outadaptertrim" ) );

	##nocontams
	my $outnoc = $f;
	$outnoc =~ s/\.(\w+)$/.nocontam\.$1/g;
	die "Can't write to $outnoc\n" unless ( open( OUTNOC, ">$outnoc" ) );

	while ( my @entry = &readFastqEntry($FHin) ) 
	{
		$#entry == 3 or die "bad read on input line $.";
		# Solexa adapter sequences. The initial G is optional because sometimes the first cycle is bad
		# And I don't use it.
		# 1) Illumina adapter
		# 2) USC Epigenome Center Me2.0 single end
		# 3) USC Epigenome Center Me2.X paired end
		if (   $entry[1] =~ /^G?ATCGGAAGAGCTCG/i
			|| $entry[1] =~ /^G?TTTGTAAGAGCTCGTA/i
			|| $entry[1] =~ /^G?TTTGTAAGAGCGGTTCAGC/i )
		{
			print OUTADAPTERS join "", @entry;			
		}

		# Now check for adapter sequence at end
		elsif ($entry[1] =~ /GATCGGAAGAGCTCG/i
			|| $entry[1] =~ /GTTTGTAAGAGCTCGTA/i
			|| $entry[1] =~ /GTTTGTAAGAGCGGTTCAGC/i )
		{
			print OUTADAPTERTRIM join "", @entry;		
		}

		# Should we check for them at the end?
		elsif ( aFrac( $entry[1] ) > $MAX_A_FRAC ) 
		{
			print OUTPOLYA join "", @entry;
		}

		#its nocontam
		else 
		{
			print OUTNOC join "", @entry;
		}
	}

	close(OUTPOLYA);
	close(OUTADAPTERS);
	close(OUTADAPTERTRIM);
	close(OUTNOC);
	close($FHin);
}

sub aFrac {
	my ($seq) = @_;

	chomp $seq;
	$seq = uc($seq);

	my $len = length($seq);
	my $nA  =
	  scalar( grep { ( $_ eq 'A' ) || ( $_ eq 'N' ) } split( //, $seq ) );

	my $frac = ( $len == 0 ) ? 0 : ( $nA / $len );

	print STDERR join( "\t", $seq, $len, $nA, $frac ) . "\n"
	  if ( $frac > $MAX_A_FRAC );
	return $frac;
}

sub readFastqEntry() 
{
	my $inFile = shift @_;
	my @entry  = ();
	for my $i ( 0 .. 3 ) 
	{
		push @entry, <$inFile> || return ();
	}
	$entry[0] =~ /^\@/ || die "Format error: entry-line 1 should start with \@ at file line $.";
	(length($entry[1]) == length($entry[3])) || die "entry-line 2 length != entry-line 4 length at file line $.";
	$entry[1] =~ /^[ACTGN]+\n$/i || die "format error for seq, unknown nucl at $.";
	return @entry;
}

