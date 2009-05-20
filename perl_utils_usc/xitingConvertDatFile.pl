#!/usr/bin/perl

use strict;
use File::Basename;
use List::Util qw(min max);

$::HUMAN = 1;
$::DELIM = "\\s+";


# simple chr, s, e
$::DELIM = "\t";
my $fld_map = 
{
    1 => 0, # Chrom
    2 => "Xiting", # source
    3 => "exon", # type
    4 => 2, # start
    5 => 3, #end
    6 => 0, # score
    7 => 0, # strand
    8 => 0, # phase
    9 => 0 # name
    };


my $fld_map_default = 
{
#    1 => "DEFAULT",
    1 => 'DEFAULT',
    2 => "DEFAULT",
    3 => "exon",
    4 => '.',
    5 => '.',
    6 => '.', # score
    7 => '.', # strand
    8 => '.', # phase
    9 => '' # name
    };


# Args
my @files = @ARGV;


foreach my $f (@files)
{
    my ($name, $path, $suf) = fileparse($f, qr/\.[^.]*/);
    print "($name)\t($path)\t($suf)\n";


#     my $global_chr = 0;
#     if ($fld_map->{1} == 'DEFAULT')
#     {
# 	die "Chromosome set to DEFAULT and can't find chrom id in filename! Quitting\n"
# 	    unless ($name =~ /(chr\d+)/i);
# 	$global_chr = lc($1);
# 	print STDERR "Using chrom $global_chr globally\n";
#     }

    # Keep output strings in a hash
    my $src_h = {};

    die "Couldn't read $f\n" unless (open(F,$f));
    my $on_line = 1;
    while (my $line = <F>)
    {
	my ($chr, $new_line) = transform_line($line, $on_line,$name);

#	print STDERR "(line $on_line) Found $chr line: $new_line\n";
	if ($chr)
	{
	    $src_h->{$chr} .= $new_line."\n";
	    $on_line++;
	}
    }
    
    # Close input file
    close(F);


    # And write
    my $outfn = $path.$name;
    $outfn .= "_transformed" if ($suf =~ /g[tf]f/i);
    $outfn .= ".gtf";
    print "Writing to $outfn\n";
    die "Can't write to $outfn\n" unless (open(OUT,">$outfn"));

# No track line
#    print OUT "track name=\'$name\' useScore=".($::SCORE_USED?1:0)." visibility=3\n";

  SRC: foreach my $src (sort { $a=~s/chr//g; $a=~s/X/23/g; $a=~s/Y/24/g; $b=~s/chr//g; $b=~s/X/23/g; $b=~s/Y/24/g; $a <=> $b } keys(%$src_h))
  { 
      print STDERR "on src chrom $src\n";
     next SRC if (!$src || ($src =~ /^\s*\#/) || ($src =~ /^\s*$/));
      print STDERR "\tprinting..\n";

      my $str = $src_h->{$src};
      print OUT $str;
  }
    close(OUT);

#     # And write output files
#   SRC: foreach my $src (keys(%$src_h))
#   {
#       next SRC if (!$src || ($src =~ /^\s*\#/) || ($src =~ /^\s*$/));

#       my $str = $src_h->{$src};
#       $src =~ s/\/\#\://g;
#       my $outfn = $path.$name."_$src".$suf;
#       print "Writing to $outfn\n";

#       if (1)
#       {
# 	  die "Can't write to $outfn\n" unless (open(OUT,">$outfn"));
# 	  print OUT $str;
# 	  close(OUT);
#       }
#   }

}


sub transform_line
{
    my ($line, $on_line, $filename) = @_;
    my $new_line = '';
    my $chr = 0;
 
#    chomp $line;
#    chop $line;
    $line =~ s/\s+$//g; # Remove any kind of line feed
#    print "line: $line\n";

    if (($line =~ /^\s*(rs)?\#/) || ($line =~ /^\s*$/))
    {
	# Empty (the rs is for the hapmap files)
    }
    else
    {
	my @orig_flds = split($::DELIM,$line);
	@orig_flds = map { s/^\"//g ; $_} @orig_flds;
	@orig_flds = map { s/\"$//g ; $_} @orig_flds;
	#print STDERR "found ".scalar(@orig_flds)." fields=".join("\t",@orig_flds)."\n";

	my @new_flds = ();
	foreach my $new_fld_i (1..9)
	{
	    my $map = $fld_map->{$new_fld_i};

	    my $negative = 0;
	    if ($map && ($map < 0))
	    {
		$negative = 1;
		$map = abs($map);
	    }

	    if (!$map)
	    {
		# Use default
		@new_flds[$new_fld_i] = $fld_map_default->{$new_fld_i};
	    }
	    elsif ($map =~ /^\d+$/)
	    {
		# map is the original fld
		@new_flds[$new_fld_i] = @orig_flds[$map-1]; # indices start at 1
	    }
	    else
	    {
		# map is the val.
		$map =~ s/\"//g; # Remove quotes
		@new_flds[$new_fld_i] = $map;
	    }

	    @new_flds[$new_fld_i] = @new_flds[$new_fld_i] * -1
		if ($negative);

	}

#	print STDERR "Coord=".@new_flds[4]."\n";
	return ($chr, $new_line) if (@new_flds[4] !~ /^\d+$/); # The line is probably a header if coord field isn't a number

	# Handle the score
	$::SCORE_USED = 2 if ((@new_flds[6] ne '.') || (!@new_flds[6]));
	@new_flds[6] = max(1,min(1000,int(@new_flds[6] * $::SCORE_ADJUST_FACTOR))) if ($::SCORE_ADJUST_FACTOR);

	if (@new_flds[1] eq 'DEFAULT')
	{
	    # For Xiting's files where the chromosome is in the
	    # filename
	    @new_flds[1] = lc($1) if ($filename =~ /(chr[0-9XY]+)/i);
	}
	elsif (@new_flds[1] !~ /^chr/i)
	{
	    @new_flds[1] = "chr" . @new_flds[1];
	}

# 	if ($::HUMAN)
# 	{
# 	    @new_flds[1] =~ s/chrX/chr23/g;
# 	    @new_flds[1] =~ s/chrY/chr24/g;
# 	}

 	if ($::HUMAN)
 	{
 	    @new_flds[1] =~ s/chr23/chrX/g;
 	    @new_flds[1] =~ s/chr24/chrY/g;
 	}


	# REVERSE backwards coordinates
	if (@new_flds[7] eq '.')
	{
	    @new_flds[7] = '+';

	    # print STDERR "Orig end: ".@new_flds[5]."\n";
	    if ((@new_flds[5] > 0) && (@new_flds[4] > @new_flds[5]))
	    {
		my $s = @new_flds[5];
		my $e = @new_flds[4];
		@new_flds[4] = $s;
		@new_flds[5] = $e;
		@new_flds[7] = '-';
	    }
	}

	# If end is <0 , interpret as width
	if (@new_flds[5] < 0)
	{
	    @new_flds[5] = @new_flds[4] + abs(@new_flds[5]) - 1;
	}

	# Make the final field proper format
	if ($::NO_IDS)
	{
	    @new_flds[9] = undef;
	}
	else
	{
	    @new_flds[9] =~ s/\#/-/g;
	    @new_flds[9] = @new_flds[2]."-".$on_line unless (@new_flds[9] =~ /./);
	    @new_flds[9] = "gene_id \"".@new_flds[9]."\"; ".
		"transcript_id \"".@new_flds[9]."\";";
	}


	$chr = @new_flds[1];
	$new_line =  join("\t", @new_flds[1..9]);
    }

    return ($chr, $new_line);
}

