#!/usr/bin/perl

use strict;


my ($datafn, $mapfn) = @ARGV;

# Read map
die "Can't read $mapfn\n" unless (open(MAP,$mapfn));
my $probe_info = {};
my $probes_found = {};
while (my $line = <MAP>)
{
    chomp $line;
    next if ($line =~ /name/);

    my ($probe, $chrom, $pos) = map {s/\"//g; $_} split(/\t/,$line);
    $probe =~ s/_x//g; # Joern said the _x has no meaning (it is Affy internal)
    $probe_info->{$probe} = [$chrom, $pos];
    $probes_found->{$probe} = -1;
}
close(MAP);
my $n_mapped = scalar(keys(%$probe_info));
print STDERR "Found $n_mapped probes mapped in $mapfn\n";


# Now data file.
die "Can't read $datafn\n" unless (open(DATA,$datafn));
my $unmapped_probes = {};
while (my $line = <DATA>)
{
    chomp $line;
    my @extra_flds;
    my @flds = split(/\t/,$line);
#    print STDERR "FLDS: " . join(",",@flds)."\n";

    if ($line !~ /EpiG/)
    {
	@extra_flds = ("chrom","pos");
    }
    else
    {
	my $probe = $flds[0];
	$probe =~ s/_at//g; # They removed these in the map file
	$probe =~ s/_x//g; # Joern said the _x has no meaning (it is Affy internal)

	my $info = $probe_info->{$probe};
	if (!$info)
	{
	    $unmapped_probes->{$probe} = 1;
	    @extra_flds = ("N/A","N/A");
	}
	else
	{
	    @extra_flds = @$info;
	    $probes_found->{$probe} = 1;
	}
    }

    my $last_fldi = scalar(@flds)-1;
    print join(",", $flds[0], @extra_flds, @flds[1..$last_fldi])."\n";
}

print STDERR "Probes in $datafn but not ${mapfn}: ".join("\n", sort(keys(%$unmapped_probes)) )."\n";
print STDERR "Probes in $mapfn but not ${datafn}: ".join("\n", sort(grep {$probes_found->{$_}<0 } keys(%$probes_found)) )."\n";

    

    
    
    

