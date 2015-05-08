import pickle

def main():
	f = open('data/reviews_train.txt', "wb")
	genres = pickle.load(open('data/raw/Genre_dictII_9500', "r"))
	#reviews = pickle.load(open('data/raw/Review_dictII_3000-8500', "r"))
	reviews = pickle.load(open('data/raw/Review_dictII_3000-8500', "r"))
	print type(genres)
	#print genres.keys()
	print type(reviews)
	i = 0
	for key in reviews.keys():
		#print key
		#print type(genres)
		if key not in genres.keys():
			continue
		temp = []
		temp.append(key)
		title = genres[key]['title']
		if title == '':
			print 'no title'
			continue 
		temp.append(title)
		genre = genreString(genres[key]['genres'])
		if genre == '': 
			print 'no genre'
			continue
		temp.append(genre)
		for record in reviews[key]['reviews']:
			if 'original_score' not in record: continue 
			score = str(getScore(record['original_score']))
			if score == '': 
				print 'no score'
				continue
			temp.append(score)
			if 'quote' not in record or record['quote'] == '': 
				print 'no quote'
				continue
			temp.append(record['quote'])
			line = ' <###> '.join(t for t in temp)
			#print line 
			f.write(line+'\n')
			del temp[3:5]
			i += 1
		#if i > 10: break
	print i
	f.close()

def genreString(genres):
	return ";".join( g for g in genres)

def getScore(str):
	#print str
	num = str.split('/')
	if len(num)!=2: return ''
	try:
		n0 = float(num[0])
		n1 = float(num[1])
	except ValueError:
		return ''
	score = n0/n1
	#print score
	return score

if __name__ == "__main__":
	main()


