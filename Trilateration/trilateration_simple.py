# Simple trilateration implementation on python.

def trilateration(x1,y1,r1,x2,y2,r2,x3,y3,r3):

# Get coefficients as in trilateration for cleaner code
  A = 2 * (x2 - x1)
  B = 2 * (y2 - y1)
  C = r1**2 - r2**2 - x1**2 + x2**2 - y1**2 + y2**2
  D = 2 * (x3 - x2)
  E = 2 * (y3 - y2)
  F = r2**2 - r3**2 - x2**2 + x3**2 - y2**2 + y3**2

# Calculate and return

  return (C*E - F*B) / (E*A - B*D) , (C*D - A*F) / (B*D - A*E)



# Get inputs and apply trilateration

if __name__ == "__main__":

	x1, y1, r1 = input("Enter x1,y1,r1: ").split()
	x2, y2, r2 = input("Enter x2,y2,r2: ").split()
	x3, y3, r3 = input("Enter x3,y3,r3: ").split()

	x,y = trilateration(float(x1), float(y1),float(r1), \
	float(x2), float(y2), float(r2), \
	float(x3), float(y3), float(r3))

	print("x,y: %.2f %.2f" %(x,y))

