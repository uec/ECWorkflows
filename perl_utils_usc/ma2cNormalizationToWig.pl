#!/usr/bin/perl

use strict;
use File::Basename(qw/basename/);

my $USAGE = "ma2cNormalizationToWig MF_probes.ndf MA2C_1967402_normalized.txt MA2C_1967402_normalized.txt MA2C_1967402_normalized.txt";

my ($unnorm_fn, @norm_fns) = @ARGV;
die "$USAGE\n" unless (@ARGV >= 2);


# Read the unnorm file
my $coords_by_xy = {};
my $chr = 0;
die "Can't read $unnorm_fn\n" unless (open(F,$unnorm_fn));
while (my $line = <F>)
{
    chomp($line);

    my @f = split(/\t/,$line);
    next if (uc($f[5]) eq 'X'); # header

    my $key = xyKey($f[5],$f[6]);
    $coords_by_xy->{$key} = $f[4];
#    print STDERR "Adding ".$f[4]." to $key\n";

    my $this_chr = $f[1];
    if ($chr && ($this_chr ne $chr))
    {
	print STDERR "Wig file can only contain one chromosome. \"$unnorm_fn\" contains both $this_chr and $chr\n";
	die;
    }
    $chr = $this_chr;
    
}
close(F);



foreach my $norm_fn (@norm_fns)
{
    # Make the filename
    my $base = basename($norm_fn, ('.txt'));
    print STDERR "Base: $base\n";

    # Start output file
    my $outfn = "${base}.wig";
    die "Couldn't write to $outfn\n" unless (open(OUTF,">$outfn"));
    
    # Start wig
    print OUTF "track type=wiggle_0 name=\"$base\"\n";
    print OUTF "variableStep\tchrom=${chr}\n";

    # And print WIG tracks
    die "Can't read $norm_fn\n" unless (open(F,$norm_fn));
    while (my $line=<F>)
    {
	chomp $line;
	my @f = split(/\t/,$line);
	if ( ($f[0]=~/^\d/) && ($f[1]=~/^\d/) )
	{
	    my $key = xyKey($f[0],$f[1]);
	    my $coord = $coords_by_xy->{$key};
	    if ($coord)
	    {
		print OUTF join("\t",$coord, $f[2])."\n";
	    }
	    else
	    {
		print STDERR "Can't find $key in $unnorm_fn\n";
	    }
	    
	}
    }
    close (F);
    close (OUTF);
}

sub xyKey
{
    my ($x,$y) = @_;
    return join(",",$x,$y);
}
