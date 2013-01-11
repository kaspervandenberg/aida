#!/usr/local/bin/perl -w


use strict;
use Getopt::Long;
use Pod::Usage;
use Shell;

my($i, $test, $seedsFile, @stopw, @seeds, $topic, $flag, $k, $patterns, $top, $topit, $tno, %hPattern, %hPatternAll, %hPatternSelected);

my $help = 0;
my $man = 0;

usage() if( !GetOptions('help|?' => \$help, man => \$man, 'm=i' => \$topit, 'n=s' => \$seedsFile, 'd=s' => \$tno, 'k=i' => \$top, 'p=s' => \$patterns) );

pod2usage(1) if $help;
pod2usage(-verbose => 2) if $man;

sub usage
 {
  print "Usage: filename.pl -m #iterations -n seeds -d data_file -k topN -p patterns\n";
  exit;
 }

open(STOPW, "stop_words.txt") or die "file with the stop words was not found\n";

$i = 0;

while(<STOPW>){
	chomp;
	$stopw[$i++] = $_;	
	}

close(STOPW);

open(SEED, $seedsFile) or die "seed file $seedsFile was not found\n";

$i = 0;

while(<SEED>){
 chomp;
 $seeds[$i++] = $_;
 
}

close(SEED);

my $tno_data;

open(TNO, $tno) or die "data file $tno was not found\n";

while(<TNO>){
	chomp;
	$tno_data = $tno_data.$_;	
	}

close(TNO);


my $out;
	
$out = $tno_data;


for(my $iter=0; $iter < $topit; $iter++){

for($i=0; $i<=$#seeds; $i++) {
	print "patterns for $seeds[$i]:\n";
	my @pat_copy;my @pat;
	if (@pat_copy = $out =~ /(\S+ +\S+ +$seeds[$i] +\S+ +\S+)/g)
	 {
		printf "we found %d patterns\n", scalar @pat_copy;
	  }
	my $pindex = 0;
	for(my $j=0; $j<=$#pat_copy; $j++){
		if ($pat_copy[$j] !~ /(\,|\:|\;|\"|\(|\)|\.|\'|\{|\}|\&|\#|\+|\%|\=|\?|\*|\@|\!|\<|\>|\~|\[|\]|\/)/) { 
		$pat[$pindex] = $pat_copy[$j]; $pindex++;
		}
	}
	printf "we found corrected %d patterns\n", scalar @pat;
	printf "we found corrected (index) $pindex\n";
	for(my $j=0; $j<=$#pat; $j++)
	 {
		$pat[$j] =~ s/(\S+ +\S+ +)$seeds[$i]( +\S+ +\S+)/$1\*$2/g;
		# create hash of patterns=>seeds the following way:
		# pattern => seed1, seed2, etc
		if (!exists($hPattern{$pat[$j]}))
			{ $hPattern{$pat[$j]}[0] = $seeds[$i];	}
		else { 
			$flag = 0;
			# check if pattern=>entity already exists
			for $k (0 .. $#{ $hPattern{$pat[$j]}})
				{ if (($hPattern{$pat[$j]}[$k] eq $seeds[$i]))		{$flag = 1;}}
			# if not, add it to the hash
			if ($flag == 0) { 
				push @{ $hPattern{$pat[$j]} }, $seeds[$i]; 
				}
		}
	 }
 }

for my $j (keys %hPattern ){
	print "seedPATT $j: @{ $hPattern{$j}}\n";
}
 
# loop over patterns in a %hPattern

for my $j (keys %hPattern ) {
	print "KEY $j \n";
	if (($j !~ /\[|\]|\\|\/|\,|\.|\;|\:|\)|\(/) && ($j =~ /(\S+ +\S+) +\* +(\S+ +\S+)/)){
		my $part1 = $1; my $part2 = $2;
		my @entities;my @entities_copy;
		if (@entities_copy = $out =~ /$part1 +(\S+) +$part2/g)
	 		{printf "we found %d entities\n", scalar @entities_copy; }
		
		my $pindex = 0;
		for($k=0; $k<=$#entities_copy; $k++){
		if ($entities_copy[$k] !~ /(\,|\:|\;|\"|\(|\)|\.|\'|\{|\}|\&|\#|\+|\%|\=|\?|\*|\@|\!|\<|\>|\~|\[|\]|\/)/) { 
			$entities[$pindex] = $entities_copy[$k]; $pindex++;
		  }
		}
		printf "we found corrected %d entities\n", scalar @entities;
		print "we found corrected (index) $pindex \n";
		# create hash of patterns=>all unique extracted entities 
		# (including seeds)
		# pattern => extracted1, extracted2, etc
		for($k=0; $k<=$#entities; $k++)
		 {
			if (!exists($hPatternAll{$j}))
				{ $hPatternAll{$j}[0] = $entities[$k];	}
	 		else { 
				$flag = 0;
				# check if pattern=>entity already exists
				for $i (0 .. $#{ $hPatternAll{$j}})
					{ if ($hPatternAll{$j}[$i] eq $entities[$k]) 
						{$flag = 1;}
					}
				# if not, add it to the hash
				if ($flag == 0) 
					{ push @{ $hPatternAll{$j} }, $entities[$k]; }
				}
		}
	}
}

for my $j (keys %hPatternAll ){
	print "pPATTERN $j\: @{ $hPatternAll{$j}}\n";
}

#calculate pattern score
for my $key (keys %hPattern ) {
	print "PATTERN $key \n";
	my $coveredEntities = $#{ $hPattern{$key}} + 1;
	my $allEntities = $#{ $hPatternAll{$key}} + 1;
	my $patternScore;
	if ($allEntities > 0) {
		$patternScore = ($coveredEntities/$allEntities)*(log($coveredEntities)/log(2))*1.000;
	if ($patternScore > 0) { $hPatternSelected{$key} = $patternScore;}
	print "patternScore is $patternScore\n";
	}
}

my @sorted = reverse sort { $hPatternSelected{$a} cmp $hPatternSelected{$b} } keys %hPatternSelected;

for (my $j=0; $j<=$#sorted; $j++)	{print "SORTED PATTERN: $sorted[$j]\n";}

open(PATT, ">>$patterns")  or die "file $patterns was not found\n";
print PATT "Added in $iter iteration\:\n";

my %hWords;

# build a hash of arrays (index) of words: word => pattern1, ..., pattern n
# %hWords
my $numPatterns = $top + $iter;

if ($numPatterns > $#sorted) { $numPatterns = $#sorted; }

for (my $j=0; $j<=$numPatterns; $j++)	
	{ print PATT "$sorted[$j]\n";
		for my $words (0 .. $#{ $hPatternAll{$sorted[$j]} }){
			if (!exists($hWords{$words}))
			{ $hWords{$hPatternAll{$sorted[$j]}[$words]}[0] = $sorted[$j];
			 }
			else {	push @{ $hWords{$hPatternAll{$sorted[$j]}[$words]} }, $sorted[$j] ;}
		}
	}

close(PATT);
# calculate word score
my %hWordScore;

open(SEED, ">>$seedsFile") or die "file $seedsFile was not found\n";

for my $key (keys %hWords ) {
	my $dummy = 0;
		for my $wordpat (0 .. $#{ $hWords{$key}}) {
			if ($#{ $hPatternAll{$hWords{$key}[$wordpat]}} > 0) {
				$dummy += log($#{ $hPatternAll{$hWords{$key}[$wordpat]}} + 2)/log(2);
			}
		}
		$dummy = $dummy / ($#{ $hWords{$key}}+1);
		if ($dummy > 0) { $hWordScore{$key} = $dummy; }
}

my @sortedWords = reverse sort { $hWordScore{$a} cmp $hWordScore{$b} } keys %hWordScore;

for (my $j=0; $j<=$#sortedWords; $j++)	{print "WORD $sortedWords[$j] \n";}

my $numWords = 0;
# print top N words to seeds file (lexicon) + add them to seeds array
my $range = 15; if ($range > $#sortedWords) { $range = $#sortedWords; }

for (my $j=0; $j<=$range; $j++)	
	{ 
	  #add unique entities only
	  my $flag = 0;
	  for(my $k=0; $k<=$#seeds; $k++)
		{
		for(my $sw=0; $sw <= $#stopw; $sw++){
			if (($seeds[$k] eq $sortedWords[$j]) || (length($sortedWords[$j])<3) || ($sortedWords[$j] eq $stopw[$sw])) { $flag = 1;}
			}
		}
	  if ($flag == 0) {
		if ($numWords < 5){
	  	print SEED "$sortedWords[$j]\n";
	  	my $index = $#seeds+1;
	  	$seeds[$index] = $sortedWords[$j];
		$numWords++;
		}
	  }
	}

for(my $j=0; $j<=$#seeds; $j++)	{
		print "ARRAY $j: *$seeds[$j]*\n";
		}

close(SEED);

}

__END__

=pod

=head1 NAME

bootstrapping_final - Bootstrapping HowTo

=head1 SYNOPSIS

bootstrapping_final [options]

Options:

	-h brief help message
	-m a number of iterations
	-n filename for a lexicon (seeds)
	-d data set
	-k number of patterns to add
	-p filename for selected patterns

=head1 DESCRIPTION

B<This program> takes topic name, number of iterations, a file with seeds, data file and a filename for the patterns as input and updates seeds' file and file with patterns in each iteration.  If a number of patterns (k) provided by a user is larger than a number of patterns with a non-zero score, all of them are added to the file with patterns.

Usage: filename.pl -m #iterations -n seeds -d data -k #patterns -p patterns
Example: bootstrapping_final.pl -m 4 -n seeds -d data -k 5 -p patterns

=cut