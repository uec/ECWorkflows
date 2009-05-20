#!/usr/bin/perl

use strict;

use File::Basename qw/basename/;

my $USAGE = "eland_filter_chr.pl [chr21 chr15 ...] unique_only [start end] eland1.txt eland2.txt";

print STDERR "$USAGE\n" unless (@ARGV>=2);


my @target_chrs = ();
my $next_arg;
while (($next_arg = shift(@ARGV)) =~ /^chr/)
{
    push(@target_chrs, lc($next_arg));
    push(@target_chrs, lc($next_arg.".fa"));
}
my $unique_only = $next_arg;

my $s = 0;
my $e = 0;
if ($ARGV[0] =~ /^\d+$/)
{
    $s = shift(@ARGV);
    $e = shift(@ARGV);
    die "$USAGE\n" unless ($e =~ /^\d+$/);
}

my @fns = @ARGV;


foreach my $fn (@fns)
{
    my $base = basename($fn, (qw/.txt/));
    die "Can't read $fn\n" unless (open(INFILE,$fn));

    my $outf = "$base";
    $outf .= ".U" if ($unique_only);

    $outf .= "." . join("-",@target_chrs[0],@target_chrs[@target_chrs-1]) if (@target_chrs);

    $outf .= ".${s}-${e}" if ($s);

    $outf .= ".processed.txt";
    die "Can't write to $outf\n" unless (open(OUTFILE,">$outf"));

    print STDERR "Processing $base\n";

    while (my $line = <INFILE>)
    {
	chomp $line;
	my ($id, $seq, $code, $n_perfect, $n_one, $n_two, $chr, $pos, $strand, $mm1, $mm2) = split(/\t/,$line);

	$chr =~ s/\.fa//g;

	if (!@target_chrs || (grep { lc($chr) eq $_  } @target_chrs))
	{
	    if (!$unique_only || (substr($code,0,1) eq 'U'))
	    {
		if (!$s || (($pos>$s)&&($pos<$e)))
		{
		    print OUTFILE $line."\n";
		}
	    }
	}
    }

    close(OUTFILE);
    close(INFILE);
}

