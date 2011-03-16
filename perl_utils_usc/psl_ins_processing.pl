#!/usr/bin/perl

# Transforms PSL output files in a very specific way.  For each query ending in _ins,
# we cut out the 2nd fragment of the alignment.  This is useful for blast/blating 
# small sequences (i.e. binding sites) by including flanking sequence delimited by
# artificial gaps. ( for instance, aaggaaggNNatcaNNggaaggaa, where atca is the region
# of interest and the aaggaa are contextual flanking seq)

use strict;

while (my $line = <>)
{
    chomp $line;
    my @flds_in = split(/\t/,$line);
    my @flds_out = @flds_in;

    my $n = @flds_in[9];

    print STDERR "Found ".scalar(@flds_in)." fields, $n\n";
    if ($n =~ /_ins$/i)
    {
	my @sizes = split(/,/,@flds_in[18]);
	my @starts_q = split(/,/,@flds_in[19]);
	my @starts_s = split(/,/,@flds_in[20]);

	my $size = @sizes[1];
	my $start_s = @starts_s[1];

	@flds_out[0] = $size;
	@flds_out[1] = 0;
	@flds_out[2] = 0;
	@flds_out[3] = 0;
	@flds_out[4] = 0;
	@flds_out[5] = 0;
	@flds_out[6] = 0;
	@flds_out[7] = 0;

	@flds_out[10] = $size;
	@flds_out[12] = $size;

	@flds_out[15] = $start_s;
	@flds_out[16] = $start_s + $size - 1;

	@flds_out[17] = 1;

	@flds_out[18] = $size.",";
	@flds_out[19] = "0".",";
	@flds_out[20] = $start_s.",";
    }

    print join("\t",@flds_out)."\n";

}

