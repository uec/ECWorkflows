#!/usr/bin/perl

use strict;
use File::Basename;
use File::Temp qw/:mktemp/;

my @files = @ARGV;


foreach my $f (@files)
{
    print STDERR "Working on $f\n";
    die "Couldn't read $f\n" unless (open(FIN,$f));
    my ($fh_out, $f_out) = mkstemp( "tempGffFileXXXXX" );

    while (my $line = <FIN>)
    {
	chomp $line;
	my @flds = split(/\t/, $line);

	if ((@flds >= 8) && (@flds[8] !~ /_id/))
	{
	    my $id = @flds[1].":".@flds[3];
	    @flds[8] = "; ".@flds[8] if (@flds[8]);
	    @flds[8] = "gene_id \"$id\"; transcript_id \"$id\"" . @flds[8];
	    $line = join("\t", @flds);
	}

	print $fh_out "${line}\n";
    }

    close($fh_out);
    close(FIN);

    `cp $f ${f}.OLD`;
    `mv $f_out $f`;
}
